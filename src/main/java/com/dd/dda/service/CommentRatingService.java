package com.dd.dda.service;

import com.dd.dda.model.CommentRatingEnum;
import com.dd.dda.model.VerificationStatus;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.idclasses.CommentRatingId;
import com.dd.dda.model.sqldata.Comment;
import com.dd.dda.model.sqldata.CommentRating;
import com.dd.dda.repository.CommentRatingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Service
public class CommentRatingService {

    private final CommentRatingRepository commentRatingRepository;
    private final CommentService commentService;
    private final UserService userService;
    private final NotificationService notificationService;

    public CommentRatingService(CommentRatingRepository commentRatingRepository, CommentService commentService, UserService userService, NotificationService notificationService) {
        this.commentRatingRepository = commentRatingRepository;
        this.commentService = commentService;
        this.userService = userService;
        this.notificationService = notificationService;
    }



    public void deleteVote(Long user_id, Long comment_id) {
        CommentRating cr = getById(user_id, comment_id);
        if(cr == null){
            throw new DDAException("CommentRating nicht gefunden");
        }
        commentRatingRepository.delete(cr);
    }

    public CommentRating getById(Long user_id, Long comment_id){

        if (user_id == null || user_id <= 0 || comment_id == null || comment_id <= 0) {
            throw new DDAException("Negative Ids are not allowed");
        }

        CommentRatingId crid = new CommentRatingId(comment_id, user_id);

        Optional<CommentRating> cro = commentRatingRepository.findById(crid);
        if (cro.isPresent()) {
            return cro.get();
        }
        return null;
    }

    public List<CommentRating> getByIdBundle(Long user_id, List<Long> comment_ids){

        List<CommentRatingId> crids = new ArrayList<>();
        for(Long comment_id : comment_ids) {
            if (user_id == null || user_id <= 0 || comment_id == null || comment_id <= 0) {
                throw new DDAException("Negative Ids are not allowed");
            }
            crids.add( new CommentRatingId(comment_id, user_id));
        }

        List<CommentRating> cros = commentRatingRepository.findAllById(crids);
        List<CommentRating> result = new ArrayList<>();
        for(int i = 0; i < comment_ids.size(); i++){
            result.add(null);
        }
        for(CommentRating cro : cros){
            int index = comment_ids.indexOf(cro.getComment_id());
            result.set(index, cro);
        }


        return result;
    }


    public CommentRating vote(Long user_id, Long comment_id, CommentRatingEnum vote) {
        Comment c = commentService.getCommentById(comment_id);
        if(c.getUser().getId() == user_id){
            throw new DDAException("Nutzer bewertet eigenes Kommentar! Bug oder Hackingverdacht!");
        }
        CommentRating cr = getById(user_id, comment_id);
        if (cr == null) {

            cr = new CommentRating(comment_id, user_id);

        }
        cr.setRating(vote);
        saveUserBilVote(cr);

        if(vote.getValue() <= 2 && userService.getUserByIdUnencrypted(user_id).getVerificationstatus() == VerificationStatus.VERIFIED) {
            notificationService.sendFirstLikeNotification(c);
        }


        return cr;



    }

    @Transactional
    public CommentRating saveUserBilVote(CommentRating commentRating){
        return commentRatingRepository.save(commentRating);
    }

    public Map<Object, Integer> getVotes(Long comment_id) {
        Map<Object, Integer> result = new HashMap<>();
        for(CommentRatingEnum cre : CommentRatingEnum.values()){
            int ys = commentRatingRepository.getNrOfVotesFor(comment_id, cre.getValue());
            result.put(cre, ys);
        }
        return result;
    }

    public List<Map<Object, Integer>> getVotesBundle(List<Long> ids) {


        List<Map<Object, Integer>> result = new ArrayList<>();
        for(int i = 0; i < ids.size(); i++){
            result.add(new HashMap<>());
        }
        for(CommentRatingEnum cre : CommentRatingEnum.values()){
            for(int i = 0; i < ids.size(); i++){
                result.get(i).put(cre, 0);
            }
            List<Object> crs = commentRatingRepository.getNrOfVotesFor(ids, cre.getValue());
            for(Object o : crs){
                Object[] u =  (Object[]) o;
                Long comment_id = ((java.math.BigInteger) u[0]).longValue(); //comment_id
                Integer count = ((java.math.BigInteger) u[1]).intValue(); //count

                int index = ids.indexOf(comment_id);
                result.get(index).put(cre, count);
            }
        }
        return result;
    }
}
