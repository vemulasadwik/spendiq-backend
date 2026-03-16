package com.spendiq.controller;

import com.spendiq.dto.response.ApiResponse;
import com.spendiq.entity.User;
import com.spendiq.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getMyNotifications(user)));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnread(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnread(user)));
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadCount(user)));
    }

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<?> dismiss(@AuthenticationPrincipal User user, @PathVariable Long id) {
        notificationService.dismiss(user, id);
        return ResponseEntity.ok(ApiResponse.ok("Notification dismissed", null));
    }

    @PatchMapping("/dismiss-all")
    public ResponseEntity<?> dismissAll(@AuthenticationPrincipal User user) {
        notificationService.dismissAll(user);
        return ResponseEntity.ok(ApiResponse.ok("All notifications dismissed", null));
    }
}