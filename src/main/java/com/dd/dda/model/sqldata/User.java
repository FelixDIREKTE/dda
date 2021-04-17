package com.dd.dda.model.sqldata;

import com.dd.dda.model.VerificationStatus;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
@Validated
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotEmpty(message = "Die E-Mail sollte gegeben werden!")
    @Pattern(regexp = ".+@.+\\..+", message = "Die E-Mail ist leider nicht korrekt!")
    @Column(name = "email")
    private String email;

    @Transient
    private String password;

    @Column(name = "password_hash")
    private String passwordHash;


    @NotNull(message = "Der Activitäts Status darf nicht leer sein")
    @Column(name = "isActive")
    private Boolean active;



    @Column(name = "Name")
    private String name;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "street")
    private String street;

    @Column(name = "housenr")
    private String housenr;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "birthdate")
    private Date birthdate;

    @Column(name = "created_time")
    private Date created_time;

    @Column(name = "Verificationstatus")
    @Enumerated(EnumType.ORDINAL)
    private VerificationStatus verificationstatus;

    @Column(name = "Admin")
    private boolean admin;


    @Column(name = "verified_by_id")
    private Long verifiedById;

    @Column(name = "verified_date")
    private Date verifiedDate;


    @Column(name = "comments_read")
    private Long comments_read;

    @Column(name = "emailverif")
    private String emailverif;

    //@Column(name = "commentrating_weight")
    //private double commentrating_weight;

    @Column(name = "avg_rating")
    private double avgRating;

    @Column(name = "commentwrite_weight")
    private double commentwrite_weight;

    @Column(name = "phonenr")
    private String phonenr;

    @Column(name = "categories_bitstring")
    private int categories_bitstring;



    public Boolean isActive() {
        return active;
    }

    public User() {
    }

    public User(Long id, String email, Boolean isActive) {
        this.id = id;
        this.email = email;
        this.active = isActive;
    }

    @Override
    public String toString() {
        return "User{\t" +
                "id=" + id +
                ", email='" + email + '\'' +
                //", password='" + password + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", street='" + street + '\'' +
                ", housenr='" + housenr + '\'' +
                ", zipcode='" + zipcode + '\'' +
                ", name='" + name + '\'' +
                ", firstname=" + firstname +
                ", birthdate=" + birthdate +
                '}';
    }

    public void updateVerificationStatus(boolean otherStuffThere) {
        if(allDataPresent() && otherStuffThere){
            if(verificationstatus == VerificationStatus.DATANEEDED){
                setVerificationstatus(VerificationStatus.WAITINGFORADMIN);
            }
        } else {
            setVerificationstatus(VerificationStatus.DATANEEDED);
        }
    }

    public boolean allDataPresent(){
        return     name != null && !name.isEmpty()
                && firstname != null && !firstname.isEmpty()
                && zipcode != null && !zipcode.isEmpty()
                && birthdate != null
                && street != null && !street.isEmpty()
                && housenr != null && !housenr.isEmpty()
                && (emailverif == null || emailverif.isEmpty());
    }
}
