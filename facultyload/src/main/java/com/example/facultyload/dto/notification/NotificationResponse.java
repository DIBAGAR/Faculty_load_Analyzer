package com.example.facultyload.dto.notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String message,
        boolean isRead,
        Instant createdAt
) {
}

