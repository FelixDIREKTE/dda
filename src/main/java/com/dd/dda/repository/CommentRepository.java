package com.dd.dda.repository;

import com.dd.dda.model.sqldata.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface CommentRepository  extends JpaRepository<Comment, Long> {


    @Query(value = "select * from comment WHERE bill_id = :bill_id and reply_comment_id = :reply_comment_id order by ranking desc", nativeQuery = true)
    List<Comment> getCommentsFor(Long bill_id, Long reply_comment_id);

    @Query(value = "select * from comment WHERE bill_id = :bill_id and user_id = :user_id order by ranking desc", nativeQuery = true)
    List<Comment> getCommentsBy(Long user_id, Long bill_id);


    @Query(value = "select * from comment WHERE bill_id = :bill_id and reply_comment_id is null and pro order by ranking desc", nativeQuery = true)
    List<Comment> getCommentsForPro(Long bill_id);

    @Query(value = "select * from comment WHERE bill_id = :bill_id and reply_comment_id is null and not pro order by ranking desc", nativeQuery = true)
    List<Comment> getCommentsForContra(Long bill_id);

    @Query(value = "select * from comment WHERE reply_comment_id = :comment_id order by ranking desc", nativeQuery = true)
    List<Comment> getRepliesFor(Long comment_id);


    @Query(value = "select count(*) from comment c inner join user u ON c.user_id = u.id WHERE u.verificationstatus = 3 and c.reply_comment_id = :comment_id", nativeQuery = true)
    Integer getNrOfRepliesFor(Long comment_id);

    @Query(value = "select c.reply_comment_id, count(c.id) from comment c inner join user u ON c.user_id = u.id WHERE u.verificationstatus = 3 and c.reply_comment_id in :comment_ids group by c.reply_comment_id", nativeQuery = true)
    List<Object> getNrOfRepliesForBundle(List<Long> comment_ids);


    @Query(value = "select c.* from comment_rating cr inner join comment c on cr.comment_id = c.id where c.bill_id = :bill_id and cr.user_id = :user_id", nativeQuery = true)
    List<Comment> getCommentsInBillRatedByUser(Long user_id, Long bill_id);

    @Modifying
    @Transactional
    @Query(value = "update comment set read_count=read_count+1 where id in :readCommentsIds", nativeQuery = true)
    void addRead(Long[] readCommentsIds);


    @Query(value = "select avg(relative_value) from comment", nativeQuery = true)
    Double getAvgLikeRatio();

    @Query(value = "select * from comment WHERE created_time > :recent and user_id = :user_id and not reply_comment_id is null", nativeQuery = true)
    List<Comment> getRepliesByAfter(Long user_id, Date recent);


    //@Query(value = "select c.*, count(cr.user_id) from comment_rating cr right outer join comment c on cr.comment_id = c.id where comment_id in :ids group by c.id", nativeQuery = true)
    //List<Object> getCommentsAndUpvotesById(List<Long> ids);


    //@Query(value = "select comment_id, count(user_id) from comment_rating where comment_id in :ids group by comment_id", nativeQuery = true)
    //List<Object> getNrOfUpvotesFor();

}
