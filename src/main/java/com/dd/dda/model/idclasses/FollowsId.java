package com.dd.dda.model.idclasses;

import java.io.Serializable;

public class FollowsId  implements Serializable {
    private Long followee_id;
    private Long follower_id;

    public FollowsId(){

    }

    public FollowsId( Long follower_id,  Long followee_id){
        this.followee_id = followee_id;
        this.follower_id = follower_id;

    }
}