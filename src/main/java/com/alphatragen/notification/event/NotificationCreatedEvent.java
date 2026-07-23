package com.alphatragen.notification.event;

import java.util.List;
import com.alphatragen.notification.domain.NotificationImportance;
import java.time.LocalDateTime;

public record NotificationCreatedEvent(
        Long notificationId,
        List<Long> recipientUserIds,
        Long apartmentId,
        String title,
        String content,
        String actionUrl,
        String eventId,
        NotificationImportance importance,
        LocalDateTime createdAt
) {
}
