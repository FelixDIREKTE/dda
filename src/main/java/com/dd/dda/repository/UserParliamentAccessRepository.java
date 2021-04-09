package com.dd.dda.repository;

import com.dd.dda.model.idclasses.UserParliamentAccessId;
import com.dd.dda.model.sqldata.UserParliamentAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserParliamentAccessRepository  extends JpaRepository<UserParliamentAccess, UserParliamentAccessId> {

    @Query(value = "select * from user_parliament_access WHERE user_id = :user_id and voteaccess", nativeQuery = true)
    List<UserParliamentAccess> findByUserId(Long user_id);
}
