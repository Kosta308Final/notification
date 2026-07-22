package com.alphatragen.notification.resolver;

import com.alphatragen.notification.domain.NotificationTargetType;

import java.util.List;

public interface TargetResolver {
    boolean supports(NotificationTargetType targetType);
    List<Long> resolve(TargetCondition condition);
}
