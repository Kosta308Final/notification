package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationImportance;

import java.time.LocalDateTime;

public record RealtimeNotificationDto(
        Long notificationId,
        String eventId,
        String title,
        String body,
        NotificationImportance importance,
        String actionUrl,
        boolean urgent,
        LocalDateTime createdAt
) {
}
