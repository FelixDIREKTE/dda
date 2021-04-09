package com.dd.dda.repository;

import com.dd.dda.model.sqldata.Parliament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ParliamentRepository extends JpaRepository<Parliament, Long> {


    @Query(value = "select * from parliament WHERE upper_parliament_id = :upperParliamentId", nativeQuery = true)
    List<Parliament> findParliamentByUpperParliamentId(Long upperParliamentId);



}