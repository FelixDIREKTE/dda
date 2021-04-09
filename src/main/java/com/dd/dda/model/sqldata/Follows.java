package com.dd.dda.model.sqldata;

import com.dd.dda.model.idclasses.FollowsId;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Validated
@Entity
@Table(name = "follows")
@IdClass(FollowsId.class)
public class Follows implements Serializable {

    @Id
    @Column(name = "follower_id")
    private Long follower_id;

    @Id
    @Column(name = "followee_id")
    private Long followee_id;


}
