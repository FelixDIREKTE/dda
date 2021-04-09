package com.dd.dda.model.sqldata;

import com.dd.dda.model.idclasses.RepresentativesBillVoteId;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Validated
@Entity
@Table(name = "representatives_bill_vote")
@IdClass(RepresentativesBillVoteId.class)
public class RepresentativesBillVote implements Serializable {

    @Id
    @Column(name = "bill_id")
    private Long bill_id;

    @Id
    @Column(name = "party_id")
    private Long party_id;


    @Column(name = "yesvotes")
    private int yesvotes;

    @Column(name = "novotes")
    private int novotes;

    @Column(name = "abstinences")
    private int abstinences;

    public RepresentativesBillVote() {
    }


    public RepresentativesBillVote(Long b, Long p) {
        bill_id = b;
        party_id = p;
    }

}
