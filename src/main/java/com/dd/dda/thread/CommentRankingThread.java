package com.dd.dda.thread;

import com.dd.dda.model.sqldata.Comment;
import com.dd.dda.repository.CommentRatingRepository;
import com.dd.dda.repository.CommentRepository;
import com.dd.dda.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Scope("prototype")
public class CommentRankingThread extends Thread{

    private List<Long> ids;
    private double avgLikeRatio;
    private CommentRepository commentRepository;
    private  CommentRatingRepository commentRatingRepository;
    private NotificationService notificationService;

    public void init(List<Long> ids, double avgLikeRatio, CommentRepository commentRepository, CommentRatingRepository commentRatingRepository, NotificationService notificationService){
        this.ids = ids;
        this.avgLikeRatio = avgLikeRatio;
        this.commentRepository = commentRepository;
        this.commentRatingRepository = commentRatingRepository;
        this.notificationService = notificationService;

    }

    @Override
    public void run() {
        log.info("start reranking thread");
        List<Comment> comments = commentRepository.findAllById(ids);

        //EntityManager ?
        List<Object> upvotes = commentRatingRepository.getNrOfUpvotesFor(ids);
        Map<Long, Long> map = new HashMap<>();
        for(Object o : upvotes){
            Object[] u =  (Object[]) o;
            Long a = ((java.math.BigInteger) u[0]).longValue();
            Long b = ((java.math.BigInteger) u[1]).longValue();
            map.put( a,b);
        }
        List<Comment> popularComments = new ArrayList<>();
        comments.stream().forEach(c -> {
            Long upv = map.get(c.getId());
            if(upv == null){
                upv = 0L;
            }
            if(c.rank(upv, avgLikeRatio)) {
                popularComments.add(c);
            }
        });
        commentRepository.saveAll(comments);
        notificationService.sendLikeNotificationBundle(popularComments);
        log.info("finish reranking thread");
    }
}
