package com.dd.dda.model.sqldata;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;

@Data
@Validated
@Entity
@Table(name = "party")
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

}
