package com.dd.dda.repository;

import com.dd.dda.model.idclasses.RepresentativesBillVoteId;
import com.dd.dda.model.sqldata.RepresentativesBillVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepresentativesBillVoteRepository extends JpaRepository<RepresentativesBillVote, RepresentativesBillVoteId> {


    @Query(value = "select * from representatives_bill_vote WHERE bill_id = :bill_id", nativeQuery = true)
    List<RepresentativesBillVote> getVotesFor(Long bill_id);
}
