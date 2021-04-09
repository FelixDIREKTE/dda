package com.dd.dda.model.sqldata;

import com.dd.dda.model.idclasses.PartyParliamentSeatsId;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Validated
@Entity
@Table(name = "party_parliament_seats")
@IdClass(PartyParliamentSeatsId.class)
public class PartyParliamentSeats implements Serializable {


    @Id
    @Column(name = "from_date")
    private Date from_date;

    @Column(name = "seats")
    private int seats;

    @Id
    @Column(name = "party_id")
    private Long party_id;

    @Id
    @Column(name = "parliament_id")
    private Long parliament_id;

    public PartyParliamentSeats() {

    }

    public PartyParliamentSeats(Long party, Long parliament, Date d) {
        from_date = d;
        party_id = party;
        parliament_id = parliament;
    }



}
