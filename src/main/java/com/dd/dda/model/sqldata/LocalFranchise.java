package com.dd.dda.model.sqldata;

import com.dd.dda.model.idclasses.LocalFranchiseId;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Validated
@Entity
@Table(name = "localFranchise")
@IdClass(LocalFranchiseId.class)
public class LocalFranchise implements Serializable {

    @Id
    @Column(name = "parliament_id")
    private Long parliament_id;

    @Id
    @Column(name = "zipcode")
    private String zipcode;

}
