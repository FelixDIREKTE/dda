package com.dd.dda.repository;

import com.dd.dda.model.idclasses.UserBillVoteId;
import com.dd.dda.model.sqldata.UserBillVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserBillVoteRepository  extends JpaRepository<UserBillVote, UserBillVoteId> {

    @Query(value = "select ubv.* from user_bill_vote ubv inner join user u inner join bill b inner join user_parliament_access upa on ubv.bill_id = b.id and ubv.user_id = u.id and upa.user_id = u.id and upa.parliament_id = b.parliament_id where b.id = :bill_id and u.VerificationStatus = 3 and upa.voteaccess", nativeQuery = true)
    List<UserBillVote> getVotesFor(Long bill_id);


    @Query(value = "select ubv.bill_id, count(ubv.user_id) from user_bill_vote ubv join user u on ubv.user_id = u.id where ubv.bill_id in :ids and ubv.vote and u.verificationstatus = 3 group by ubv.bill_id", nativeQuery = true)
    List<Object> getNrOfUpvotesFor(List<Long> ids);

    @Query(value = "select ubv.bill_id, count(ubv.user_id) from user_bill_vote ubv join user u on ubv.user_id = u.id where ubv.bill_id in :ids and not ubv.vote and u.verificationstatus = 3 group by ubv.bill_id", nativeQuery = true)
    List<Object> getNrOfDownvotesFor(List<Long> ids);


    @Query(value = "select user_id from user_bill_vote WHERE bill_id = :bill_id ", nativeQuery = true)
    List<Long> findIdsOfUsersWhoVotedOn(Long bill_id);

    @Query(value = "select ubv.bill_id, count(ubv.user_id) from user_bill_vote ubv join user u on ubv.user_id = u.id where ubv.bill_id in :bill_ids and ubv.vote = :v and u.verificationstatus = 3 group by ubv.bill_id", nativeQuery = true)
    List<Object> getNrOfVotesFor(List<Long> bill_ids, Boolean v);
}
