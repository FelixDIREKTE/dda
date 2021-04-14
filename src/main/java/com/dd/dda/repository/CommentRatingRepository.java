package com.dd.dda.repository;

import com.dd.dda.model.idclasses.CommentRatingId;
import com.dd.dda.model.sqldata.CommentRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRatingRepository  extends JpaRepository<CommentRating, CommentRatingId> {

    @Query(value = "select cr.* from comment_rating cr join user u on cr.user_id = u.id where cr.comment_id = :comment_id and u.verificationstatus = 3", nativeQuery = true)
    List<CommentRating> getVotesFor(Long comment_id);


    @Query(value = "select count(*) from comment_rating cr join user u on cr.user_id = u.id where cr.comment_id = :comment_id and cr.rating = :crevalue and u.verificationstatus = 3", nativeQuery = true)
    Integer getNrOfVotesFor(Long comment_id, int crevalue);


    @Query(value = "select cr.comment_id, count(cr.user_id) from comment_rating cr join user u on cr.user_id = u.id where cr.comment_id in :comment_ids and cr.rating = :crevalue and u.verificationstatus = 3 group by cr.comment_id", nativeQuery = true)
    List<Object> getNrOfVotesFor(List<Long> comment_ids, int crevalue);




    @Query(value = "select cr.comment_id, count(cr.user_id) from comment_rating cr join user u on cr.user_id = u.id where cr.comment_id in :ids and u.verificationstatus = 3 group by cr.comment_id", nativeQuery = true)
    List<Object> getNrOfUpvotesFor(List<Long> ids);

}
