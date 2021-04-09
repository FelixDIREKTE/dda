package com.dd.dda.repository;

import com.dd.dda.model.sqldata.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface BillRepository  extends JpaRepository<Bill, Long> {



    @Query(value = "select * from bill WHERE parliament_id = :parliament_id and parliament_role = :parliament_role order by ranking desc", nativeQuery = true)
    List<Bill> getBillsForParliamentOrderedByRanking(Long parliament_id, Long parliament_role);

    @Query(value = "SELECT * FROM bill WHERE MATCH (Name,abstract) AGAINST (:searchterm) and  parliament_id = :parliament_id and parliament_role = :parliament_role order by MATCH (Name,abstract) AGAINST (:searchterm) desc;", nativeQuery = true)
    List<Bill> getBillsForParliamentOrderedBySearchTerm(String searchterm, Long parliament_id, Long parliament_role);


    @Query(value = "select * from bill WHERE parliament_id = :parliament_id and parliament_role = :parliament_role and date_vote <= :today order by date_vote desc", nativeQuery = true)
    List<Bill> getPastBillsForParliamentOrderedByDateVote(Long parliament_id, Long parliament_role, Date today);

    @Query(value = "select * from bill WHERE parliament_id = :parliament_id and parliament_role = :parliament_role and date_vote > :today  order by date_vote asc", nativeQuery = true)
    List<Bill> getFutureBillsForParliamentOrderedByDateVote(Long parliament_id, Long parliament_role, Date today);

    @Query(value = "select * from bill WHERE date_vote = :day", nativeQuery = true)
    List<Bill> getBillsDueOn(Date day);



    @Modifying
    @Transactional
    @Query(value = "update bill set read_count=read_count+1 where id in :readBillsIds", nativeQuery = true)
    void addRead(Long[] readBillsIds);

    @Modifying
    @Transactional
    @Query(value = "update bill set read_detail_count=read_detail_count+1 where id = :readBillDetailId", nativeQuery = true)
    void addDetailRead(Long readBillDetailId);


    @Query(value = "select * from bill WHERE parliament_id = :parliament_id and created_by_id = :user_id and parliament_role = :parliament_role order by ranking desc", nativeQuery = true)
    List<Bill> getBillsBy(Long user_id, Long parliament_id, int parliament_role);

    @Query(value = "select avg(relative_value) from bill", nativeQuery = true)
    Double getAvgLikeRatio();


    @Query(value = "select b.* from user_bill_vote ubv inner join bill b on ubv.bill_id = b.id where b.parliament_id = :parliament_id and b.parliament_role = :parliament_role and ubv.user_id = :user_id", nativeQuery = true)
    List<Bill> getBillsInParliamentVotedByUser(Long user_id, Long parliament_id, int parliament_role);

}
