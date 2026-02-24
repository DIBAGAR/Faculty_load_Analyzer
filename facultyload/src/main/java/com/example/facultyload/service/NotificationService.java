package com.example.facultyload.service;

import com.example.facultyload.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification notifyUser(Long userId, String message);
    List<Notification> myLatest(String email);
    void markRead(Long notificationId, String email);
    long myUnreadCount(String email);
}

