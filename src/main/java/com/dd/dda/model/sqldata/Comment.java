package com.dd.dda.model.sqldata;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Validated
@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    private Bill bill;

    @OneToOne
    private User user;

    @Column(name = "text")
    private String text;

    @Column(name = "created_time")
    private Date created_time;

    @Column(name = "read_count")
    private long readCount;

    @OneToOne
    private Comment reply_comment;

    @Column(name = "ranking")
    private double ranking;

    @Column(name = "relative_value")
    private double relative_value;

    @Column(name = "pro")
    private boolean pro;

    public double getCustomRanking() {
        return customRanking;
    }

    public void setCustomRanking(double customRanking) {
        this.customRanking = customRanking;
    }

    @Transient
    private double customRanking;



    @Override
    public String toString() {
        return "Comment{\t" +
                "id=" + id +
                ", bill='" + bill.getId() + '\'' +
                ", user='" + user.getId() + '\'' +
                ", text='" + text + '\'' +
                ", created_time='" + created_time + '\'' +
                ", reply_comment='" + reply_comment + '\'' +
                ", pro='" + pro + '\'' +
                '}';
    }

    public boolean rank(Long posRatings, double avgValue){
        boolean prevNotPopular = ranking < 1.5 * avgValue;

        if(readCount == 0){
            relative_value = 0;
        } else {
            relative_value = 1.0 * posRatings / readCount;
        }
        int k;
        if(avgValue < 0.05){
            k = 80;
        } else {
            k = (int) (4.0 / avgValue);
        }
        if(readCount >= k){
            ranking = relative_value;
        } else {
            double p = readCount * 1.0 / k;
            ranking = p * relative_value + (1.0 - p) * avgValue;
        }
        if(prevNotPopular && ranking > 1.5 * avgValue){
            return true;
        }
        return false;
    }

    public void customRank(List<Long> followingIds) {
        boolean follows = followingIds.contains(user.getId() );
        if(follows){
            customRanking = ranking + 0.3;
        } else {
            customRanking = ranking;
        }
    }
}
