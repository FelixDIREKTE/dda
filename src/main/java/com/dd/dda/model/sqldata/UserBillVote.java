package com.dd.dda.model.sqldata;

import com.dd.dda.model.idclasses.UserBillVoteId;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Validated
@Entity
@Table(name = "user_bill_vote")
@IdClass(UserBillVoteId.class)
public class UserBillVote implements Serializable {

    @Id
    @Column(name = "bill_id", insertable = false, updatable = false)
    private Long bill_id;

    @Id
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long user_id;


    @Column(name = "vote")
    private boolean vote;

    public UserBillVote() {

    }

    public UserBillVote(Long bill_id, Long user_id) {
        this.bill_id = bill_id;
        this.user_id = user_id;
    }

    public boolean getVote() {
        return vote;
    }
}
