package com.spendiq.repository;

import com.spendiq.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
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
    void deleteByGroupSplitId(Long groupSplitId);
}
