package com.dd.dda.model.idclasses;

import java.io.Serializable;

public class UserParliamentAccessId implements Serializable {


    private Long parliament_id;
    private Long user_id;

    public UserParliamentAccessId() {
    }


    public UserParliamentAccessId(Long p, Long u) {
        parliament_id = p;
        user_id = u;
    }

}
