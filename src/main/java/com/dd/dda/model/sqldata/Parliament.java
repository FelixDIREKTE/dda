package com.dd.dda.model.sqldata;


import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Data
@Validated
@Entity
@Table(name = "parliament")
public class Parliament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "Der Name sollte gegeben sein!")
    @Column(name = "name")
    private String Name;

    @Column(name = "level")
    private byte level;

    @OneToOne
    private Parliament upper_parliament;


    @Override
    public String toString() {
        return Name;
    }

}
