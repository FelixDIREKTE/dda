package com.dd.dda.repository;

import com.dd.dda.model.idclasses.PartyParliamentSeatsId;
import com.dd.dda.model.sqldata.PartyParliamentSeats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PartyParliamentSeatsRepository  extends JpaRepository<PartyParliamentSeats, PartyParliamentSeatsId> {


    @Query(value = "select * from party_parliament_seats WHERE parliament_id = :parliament_id", nativeQuery = true)
    List<PartyParliamentSeats> getSeatsFor(Long parliament_id);
}
