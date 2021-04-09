package com.dd.dda.repository;

import com.dd.dda.model.sqldata.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByEmailAndActiveTrue(String email);


    @Query("select count(*) from User as u where u.verificationstatus=1")
    int nrOfWaitingUsers();

    @Query(value = "select * from user where verificationstatus=1 limit 1", nativeQuery = true)
    User waitingUser();

    @Query("select count(*) from User as u where u.verificationstatus=2")
    int nrOfWaitingUsers2();

    @Query(value = "select * from user where verificationstatus=2 limit 1", nativeQuery = true)
    User waitingUser2();

    @Modifying
    @Transactional
    @Query(value = "update user set comments_read=comments_read + :amount where id = :id", nativeQuery = true)
    void addCommentsRead(Long id, int amount);


    @Query(value = "select * from user where verificationstatus=0 and created_time > :d2 and created_time < :d3", nativeQuery = true)
    List<User> getUsersCreatedBetween(Date d2, Date d3);
}