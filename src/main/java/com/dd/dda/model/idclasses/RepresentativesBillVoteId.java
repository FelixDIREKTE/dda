package com.dd.dda.model.idclasses;

import java.io.Serializable;

public class RepresentativesBillVoteId implements Serializable {

    private Long bill_id;
    private Long party_id;

    public RepresentativesBillVoteId() {
    }


    public RepresentativesBillVoteId(Long b, Long p) {
        bill_id = b;
        party_id = p;
    }

}
