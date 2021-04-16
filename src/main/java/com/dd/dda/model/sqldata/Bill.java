package com.dd.dda.model.sqldata;

import lombok.Data;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.util.Date;

@Data
@Validated
@Entity
@Table(name = "bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "abstract")
    private String abstr;

    //@Column(name = "draftbillPath")
    //private String draftbillPath;

    //@Column(name = "categorie")
    //private String categorie;

    @Column(name = "resolutionrecommendation")
    private String resolutionrecommendation;

    @Column(name = "procedurekey")
    private String procedurekey;

    @Column(name = "billtype")
    private String billtype;

    //@Column(name = "billApplicationPath")
    //private String billApplicationPath;

    @Column(name = "date_presented")
    private Date date_presented;

    @Column(name = "date_vote")
    private Date date_vote;

    @Column(name = "read_count")
    private long readCount;

    @Column(name = "read_detail_count")
    private long read_detail_count;

    @Column(name = "relative_value")
    private double relative_value;

    @Column(name = "ranking")
    private double ranking;

    @Getter
    @Column(name = "created_time")
    private Date created_time;

    @OneToOne
    private User created_by;

    @OneToOne
    private Party party;

    @OneToOne
    private Parliament parliament;

    @Column(name = "parliament_role")
    private int parliament_role;

    @Column(name = "status")
    private int status;

    @Column(name = "final_yes_votes")
    private int final_yes_votes;

    @Column(name = "final_no_votes")
    private int final_no_votes;

    @Column(name = "categories_bitstring")
    private int categories_bitstring;

    public double getCustomRanking() {
        return customRanking;
    }

    @Transient
    private double customRanking;


    public void rank(Long upv, double avgLikeRatio) {
        if(parliament_role == 1){
            //bei Initiative
            ranking = upv;
            relative_value = upv;
        } else {
            if (readCount == 0) {
                relative_value = 0;
            } else {
                if (parliament_role == 2) {
                    //bei Diskussionen ziehen negative Stimmen runter
                    relative_value = (1.0 * upv + 0.2 * read_detail_count) / readCount;
                } else {
                    relative_value = (1.0 * upv + 0.2 * read_detail_count) / readCount;
                }
            }
            int k;
            if (avgLikeRatio < 0.05) {
                k = 80;
            } else {
                k = (int) (4.0 / avgLikeRatio);
            }
            if (readCount >= k) {
                ranking = relative_value;
            } else {
                double p = readCount * 1.0 / k;
                ranking = p * relative_value + (1.0 - p) * avgLikeRatio;
            }
        }
    }

    private int q2(int a){
        int b = a;
        int r = 0;
        while(b > 0){
            if(b % 2 == 1){
                r++;
            }
            b = b >> 1;
        }
        return r;
    }

    private double categBonus(User u){
        if(categories_bitstring == 0){
            return 0.0;
        }
        int mutualCateg = u.getCategories_bitstring() & categories_bitstring;
        return 0.7 * q2(mutualCateg) / q2(categories_bitstring);
    }

    public void customRank(User u) {
        double categBonus = categBonus(u);
        customRanking = ranking + categBonus;
    }

    // 86.400.000 ms = 1 Tag

    public void customRankFutureBills(User u, Date today) {
        double categBonus = categBonus(u);
        long timeleft = date_vote == null ? 8640000000L : date_vote.getTime() - today.getTime(); //100 Tage
        double timeBonus = 86400000.0 /*1Tag*/ / (timeleft + 86400000 /*1 Tag */ ) ;
        customRanking = ranking + categBonus + timeBonus;
    }

    public void customRankPastBills(User u, Date today) {
        double categBonus = categBonus(u);
        long timeleft = date_vote == null ? -8640000000L : date_vote.getTime() - today.getTime();
        double timeBonus = timeleft / 864000000.0; //10 Tage = 1 Ranking weniger
        customRanking = ranking + categBonus + timeBonus;

    }

    public void customRankDiscussion(User u) {
        double categBonus = categBonus(u);
        customRanking = ranking + categBonus;
    }

    public void customRankInitiatives(User u) {
    }
}
