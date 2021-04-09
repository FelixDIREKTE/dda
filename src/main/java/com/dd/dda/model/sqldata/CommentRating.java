package com.dd.dda.model.sqldata;

import com.dd.dda.model.CommentRatingEnum;
import com.dd.dda.model.idclasses.CommentRatingId;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Validated
@Entity
@Table(name = "comment_rating")
@IdClass(CommentRatingId.class)
public class CommentRating implements Serializable {

    @Id
    @Column(name = "comment_id")
    private Long comment_id;

    @Id
    @Column(name = "user_id")
    private Long user_id;

    @Column(name = "rating")
    @Enumerated(EnumType.ORDINAL)
    private CommentRatingEnum rating;


    public CommentRating(Long comment_id, Long user_id) {
        this.comment_id = comment_id;
        this.user_id = user_id;
    }

    public CommentRating() {

    }
}
