package com.dd.dda.model.idclasses;

import java.io.Serializable;
import java.util.Date;

public class PartyParliamentSeatsId implements Serializable {

    private Date from_date;
    private Long party_id;
    private Long parliament_id;

    public PartyParliamentSeatsId() {
    }


    public PartyParliamentSeatsId(Long party, Long parliament, Date d) {
        from_date = d;
        party_id = party;
        parliament_id = parliament;
    }
}
