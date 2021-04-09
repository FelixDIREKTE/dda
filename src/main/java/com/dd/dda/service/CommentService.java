package com.dd.dda.service;

import com.dd.dda.config.ThreadConfig;
import com.dd.dda.model.VerificationStatus;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.model.sqldata.Comment;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.repository.CommentRatingRepository;
import com.dd.dda.repository.CommentRepository;
import com.dd.dda.thread.CommentRankingThread;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentService {

    private  final CommentRepository commentRepository;
    private final BillService billService;
    private final UserService userService;
    private final Set<Long> commentIdsToRerank;
    private final CommentRatingRepository commentRatingRepository;
    private final NotificationService notificationService;
    private final HtmlConverterService htmlConverterService;
    private final FollowsService followsService;

    public Double getAvgLikeRatio() {
        if(avgLikeRatio == null){
            updateAvgLikeRatio();
        }
        return avgLikeRatio;
    }

    public void updateAvgLikeRatio() {
        Double res = commentRepository.getAvgLikeRatio();
        if(res == null){
            res = 0.;
        }
        setAvgLikeRatio( res );
    }

    public void setAvgLikeRatio(Double avgLikeRatio) {
        this.avgLikeRatio = avgLikeRatio;
    }

    private Double avgLikeRatio;

    public CommentService(CommentRepository commentRepository, BillService billService, UserService userService, CommentRatingRepository commentRatingRepository, NotificationService notificationService, HtmlConverterService htmlConverterService, FollowsService followsService) {
        this.commentRepository = commentRepository;
        this.billService = billService;
        this.userService = userService;
        this.commentRatingRepository = commentRatingRepository;
        this.notificationService = notificationService;
        this.htmlConverterService = htmlConverterService;
        this.followsService = followsService;
        this.commentIdsToRerank = new ConcurrentHashSet<Long>();
    }

    public List<Comment> getRankedComments(Long user_id , Long bill_id, Long reply_comment_id) {
        List<Comment> result;
        if(reply_comment_id == -1){
            result = commentRepository.getCommentsForPro(bill_id).stream().
                    filter(c -> c.getUser().getVerificationstatus() == VerificationStatus.VERIFIED
                             )
                    .collect(Collectors.toList());
        } else {
            if (reply_comment_id == -2){
                result = commentRepository.getCommentsForContra(bill_id).stream().
                        filter(c -> c.getUser().getVerificationstatus() == VerificationStatus.VERIFIED
                                )
                        .collect(Collectors.toList());

            } else {
                if(reply_comment_id > 0) {
                    result = commentRepository.getCommentsFor(bill_id, reply_comment_id).stream().
                            filter(c -> c.getUser().getVerificationstatus() == VerificationStatus.VERIFIED)
                            .collect(Collectors.toList());
                } else {
                    throw new DDAException("Ungültige reply_comment_id " + reply_comment_id);
                }
            }
        }
        List<Long> followingIds = followsService.getFollowindgIds(user_id);
        followingIds.add(user_id);
        result.stream().forEach(c -> c.customRank(followingIds));
        result = result.stream().sorted(Comparator.comparingDouble(Comment::getCustomRanking)).collect(Collectors.toList());
        Collections.reverse(result);

        return result;


    }

    public Comment createComment(Long id, Long bill_id, String text, Long replied_comment_id, boolean pro) {
        Comment comment = new Comment();
        comment.setCreated_time(new Date());
        comment.setText(htmlConverterService.stringToHtml(text));
        Comment replied = null;
        if(replied_comment_id != null) {
            replied = getCommentById(replied_comment_id);
            if(replied == null){
                throw new DDAException("Reply-Comment nicht gefunden");
            }
            comment.setReply_comment(replied);
        }
        Bill bill = billService.getBillById(bill_id);
        if(bill == null){
            throw new DDAException("Bill nicht gefunden");
        }
        comment.setBill(bill);
        User user = userService.getUserByIdUnencrypted(id);
        if(user == null){
            throw new DDAException("User nicht gefunden");
        }
        comment.setUser(user);
        comment.setPro(pro);
        comment.setRelative_value(0);
        comment.setRanking(getAvgLikeRatio());
        Comment result = commentRepository.save(comment);
        if(replied_comment_id != null && replied.getUser().getId() != id && user.getVerificationstatus() == VerificationStatus.VERIFIED){
            notificationService.sendReplyNotification(result);
        }
        return result;
    }


    public Comment getCommentById(Long id) {
        if (id == null || id <= 0) {
            throw new DDAException("Negative Ids are not allowed");
        }

        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            Comment userFound = comment.get();
            return userFound;
        }
        return null;
    }

    public String update(Long id, Long comment_id, String text) {
        Comment comment = getCommentById(comment_id);

        if(id != comment.getUser().getId()){
            throw  new DDAException("Keine Berechtigung!");
        }
        if(text != null && !text.isEmpty()) {
            comment.setText(text);
        }
        commentRepository.save(comment);
        return "ok";
    }

    public void delete(Long user_id, Long comment_id) {
        Comment comment = getCommentById(comment_id);
        if(user_id != comment.getUser().getId()){
            throw  new DDAException("Keine Berechtigung!");
        }
        commentRepository.delete(comment);

    }

    public int getNrOfReplies(Long comment_id) {
        return commentRepository.getNrOfRepliesFor(comment_id);
    }

    public List<Long> getCommentIdsOfRatingsForBill(Long id, Long bill_id) {
        List<Comment> ratedComments = commentRepository.getCommentsInBillRatedByUser(id, bill_id);
        List<Comment> ownComments = commentRepository.getCommentsBy(id, bill_id);

        List<Comment> result = new ArrayList<>();

        for(Comment c : ratedComments){
            Comment curr = c;
            while(curr != null){
                result.add(curr);
                curr = curr.getReply_comment();
            }
        }
        for(Comment c : ownComments){
            Comment curr = c;
            while(curr != null){
                result.add(curr);
                curr = curr.getReply_comment();
            }
        }
        return result.stream().map(c -> c.getId()).distinct().collect(Collectors.toList());
    }

    public void addReads(Long[] readCommentsIds) {
        commentRepository.addRead(readCommentsIds);
        commentIdsToRerank.addAll(Arrays.asList(readCommentsIds));
    }



    public void startCommentRankingThread(){
        if(!commentIdsToRerank.isEmpty()) {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadConfig.class);
            CommentRankingThread commentRankingThread = (CommentRankingThread) ctx.getBean("commentRankingThread");

            //List<Long> ids = commentRepository.findAll().stream().map(c -> c.getId()).collect(Collectors.toList());
            List<Long> ids = new ArrayList<>(commentIdsToRerank);
            commentIdsToRerank.clear();
            commentRankingThread.init(ids, getAvgLikeRatio(), commentRepository, commentRatingRepository, notificationService);
            commentRankingThread.start();
        }
    }

    public void sendNotificationsForRecentReplies(Long verifiedId) {
        Date recent = DateUtils.addDays(new Date(), -6);
        List<Comment> recentReplies = commentRepository.getRepliesByAfter(verifiedId, recent).stream().filter(c -> c.getReply_comment().getUser().getId() != verifiedId).collect(Collectors.toList());
        notificationService.sendReplyNotificationBundle(recentReplies);
    }

    public List<Integer> getNrOfRepliesBundle(List<Long> ids) {

        List<Object> rs = commentRepository.getNrOfRepliesForBundle(ids);

        List<Integer> result = new ArrayList<>(ids.size());
        for(int i = 0; i < ids.size(); i++){
            result.add(0);
        }

        for(Object o : rs){
            Object[] u =  (Object[]) o;
            Long comment_id = ((java.math.BigInteger) u[0]).longValue(); //comment_id
            Integer count = ((java.math.BigInteger) u[1]).intValue(); //count

            int index = ids.indexOf(comment_id);
            result.set(index, count);
        }
        return result;


    }
}
