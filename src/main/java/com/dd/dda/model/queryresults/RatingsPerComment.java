package com.dd.dda.model.queryresults;

public class RatingsPerComment {

    public Long getComment_id() {
        return comment_id;
    }

    public void setComment_id(Long comment_id) {
        this.comment_id = comment_id;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public RatingsPerComment(Long comment_id, Long count){
        this.comment_id = comment_id;
        this.count = count;
    }

    //@Column(name="comment_id")
    private Long comment_id;

    //@Column(name="count(user_id)")
    private Long count;


}
