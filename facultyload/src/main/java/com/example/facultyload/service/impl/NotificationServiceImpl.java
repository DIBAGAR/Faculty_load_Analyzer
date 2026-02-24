package com.example.facultyload.service.impl;

import com.example.facultyload.entity.Notification;
import com.example.facultyload.entity.User;
import com.example.facultyload.exception.ForbiddenException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.NotificationRepository;
import com.example.facultyload.repository.UserRepository;
import com.example.facultyload.service.MailService;
import com.example.facultyload.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Override
    @Transactional
    public Notification notifyUser(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Notification saved = notificationRepository.save(Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build());
        try {
            mailService.send(user.getEmail(), "Faculty Load Notification", message);
        } catch (Exception ignored) {
            // In-app notification still succeeds if mail fails/misconfigured.
        }
        return saved;
    }

    @Override
    public List<Notification> myLatest(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        return notificationRepository.findTop50ByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found: " + notificationId));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Cannot modify other user's notifications");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    public long myUnreadCount(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }
}

