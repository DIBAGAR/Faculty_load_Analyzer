package com.abc.facultyload.service;

import com.abc.facultyload.entity.Notification;
import com.abc.facultyload.entity.User;
import com.abc.facultyload.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Async("taskExecutor")
    @Transactional
    public void createAndSendNotification(User user, String title, String message, boolean sendEmail) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .readStatus(false)
                .build();
        notificationRepository.save(notification);

        if (sendEmail && user.getEmail() != null) {
            String htmlBody = "<h3>" + title + "</h3><p>" + message + "</p>";
            emailService.sendHtmlEmail(user.getEmail(), "Faculty Load Analyzer: " + title, htmlBody);
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadStatusFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setReadStatus(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findAllByUserIdAndReadStatusFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setReadStatus(true));
        notificationRepository.saveAll(unread);
    }
}
