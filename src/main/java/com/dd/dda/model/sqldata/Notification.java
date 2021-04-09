package com.dd.dda.model.sqldata;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.util.Date;

@Data
@Validated
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //@OneToOne
    //private User receiverUser; //receiver_user_id

    @Column(name = "receiver_user_id")
    private Long receiver_user_id;

    @Column(name = "created_time")
    private Date created_time;

    @Column(name = "message")
    private String message;

    @Column(name = "link")
    private String link;

    @Column(name = "noted")
    private boolean noted;

}
