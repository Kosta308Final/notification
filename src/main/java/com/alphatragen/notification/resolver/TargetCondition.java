package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;

public record TargetCondition(
        NotificationTargetType targetType,
        Long apartmentId,
        Long userId,
        String building,
        String unit,
        String role) {
}
