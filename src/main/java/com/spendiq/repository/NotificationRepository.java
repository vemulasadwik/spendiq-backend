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

    // All notifications for a user, newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Unread (not dismissed) notifications only
    List<Notification> findByUserIdAndDismissedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndDismissedFalse(Long userId);

    // Delete all notifications for a split (used when deleting a split)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.groupSplit.id = :splitId")
    void deleteByGroupSplitId(@Param("splitId") Long splitId);

    @Modifying
    @Query(value = "DELETE FROM notifications WHERE group_split_id = :splitId", nativeQuery = true)
    void deleteByGroupSplitIdNative(@Param("splitId") Long splitId);
}
