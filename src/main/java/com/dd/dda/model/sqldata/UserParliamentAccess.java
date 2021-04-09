package com.dd.dda.model.sqldata;

import com.dd.dda.model.idclasses.UserParliamentAccessId;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Validated
@Entity
@Table(name = "user_parliament_access")
@IdClass(UserParliamentAccessId.class)
public class UserParliamentAccess implements Serializable  {


    @Id
    @Column(name = "parliament_id", insertable = false, updatable = false)
    private Long parliament_id;

    @Id
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long user_id;


    @Column(name = "voteaccess")
    private boolean voteaccess;

    public UserParliamentAccess() {

    }

    public UserParliamentAccess(Long parliament_id, Long user_id) {
        this.parliament_id = parliament_id;
        this.user_id = user_id;
    }
}


