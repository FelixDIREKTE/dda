package com.dd.dda.model.idclasses;

import java.io.Serializable;

public class UserBillVoteId  implements Serializable {


    private Long bill_id;
    private Long user_id;

    public UserBillVoteId() {
    }


    public UserBillVoteId(Long b, Long u) {
        bill_id = b;
        user_id = u;
    }
}
