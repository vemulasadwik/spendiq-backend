package com.spendiq.repository;

import com.spendiq.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndDismissedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndDismissedFalse(Long userId);

    // ✅ Uses correct column name: split_id (not group_split_id)
    @Modifying
    @Query(value = "DELETE FROM notifications WHERE split_id = :splitId", nativeQuery = true)
    void deleteNotificationsBySplitId(@Param("splitId") Long splitId);
}
