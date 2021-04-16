package com.dd.dda.repository;

import com.dd.dda.model.sqldata.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface NotificationRepository   extends JpaRepository<Notification, Long> {

    @Query(value = "select * from notification WHERE receiver_user_id = :user_id order by created_time desc", nativeQuery = true)
    List<Notification> getNotifications(Long user_id);


    @Modifying
    @Transactional
    @Query(value = "delete from notification WHERE created_time < :d2 OR ( created_time < :d1 AND noted )", nativeQuery = true)
    void deleteOldNotifications(Date d1, Date d2);

    @Modifying
    @Transactional
    @Query(value = "update notification set noted = true WHERE id in :readNotificationsIds", nativeQuery = true)
    void markReadNotifications(List<Long> readNotificationsIds);

    @Query(value = "select * from notification WHERE receiver_user_id = :user_id and message = :msg", nativeQuery = true)
    List<Notification> findByMsg(Long user_id, String msg);
}
