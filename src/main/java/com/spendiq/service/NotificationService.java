package com.spendiq.service;

import com.spendiq.dto.response.NotificationResponse;
import com.spendiq.entity.Notification;
import com.spendiq.entity.User;
import com.spendiq.exception.ResourceNotFoundException;
import com.spendiq.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getMyNotifications(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationResponse::from).collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnread(User user) {
        return notificationRepository.findByUserIdAndDismissedFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationResponse::from).collect(Collectors.toList());
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndDismissedFalse(user.getId());
    }

    @Transactional
    public void dismiss(User user, Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notif.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notif.dismiss();
        notificationRepository.save(notif);
    }

    @Transactional
    public void dismissAll(User user) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndDismissedFalseOrderByCreatedAtDesc(user.getId());
        unread.forEach(Notification::dismiss);
        notificationRepository.saveAll(unread);
    }
}