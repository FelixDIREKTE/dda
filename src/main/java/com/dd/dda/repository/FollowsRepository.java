package com.dd.dda.repository;

import com.dd.dda.model.idclasses.FollowsId;
import com.dd.dda.model.sqldata.Follows;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FollowsRepository  extends JpaRepository<Follows, FollowsId> {

    @Query(value = "select followee_id from follows WHERE follower_id = :id", nativeQuery = true)
    List<Long> getFollowingIds(Long id);

}
