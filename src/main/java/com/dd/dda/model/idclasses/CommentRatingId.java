package com.dd.dda.model.idclasses;

import java.io.Serializable;

public class CommentRatingId implements Serializable {

    private Long comment_id;
    private Long user_id;

    public CommentRatingId(Long comment_id, Long user_id) {
        this.comment_id = comment_id;
        this.user_id = user_id;
    }

    public CommentRatingId() {

    }

}
