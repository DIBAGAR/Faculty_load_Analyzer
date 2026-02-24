package com.example.facultyload.controller;

import com.example.facultyload.dto.notification.NotificationResponse;
import com.example.facultyload.entity.Notification;
import com.example.facultyload.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> myLatest(Authentication authentication) {
        return notificationService.myLatest(authentication.getName()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/unread-count")
    public long unreadCount(Authentication authentication) {
        return notificationService.myUnreadCount(authentication.getName());
    }

    @PostMapping("/{notificationId}/read")
    public void markRead(@PathVariable Long notificationId, Authentication authentication) {
        notificationService.markRead(notificationId, authentication.getName());
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getMessage(), Boolean.TRUE.equals(n.getIsRead()), n.getCreatedAt());
    }
}

