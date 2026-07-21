package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationRecipient;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.resolver.NotificationTargetResolverComposite;
import com.alphatragen.notification.resolver.TargetCondition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationRecipientCreator {

    private final NotificationTargetResolverComposite targetResolverComposite;

    public NotificationRecipientCreator(NotificationTargetResolverComposite targetResolverComposite) {
        this.targetResolverComposite = targetResolverComposite;
    }

    public List<Long> create(Notification notification, NotificationEventDto dto) {
        List<Long> recipientUserIds = targetResolverComposite.resolveTargets(new TargetCondition(
                dto.getTargetType(),
                dto.getApartmentId(),
                dto.getUserId(),
                dto.getBuilding(),
                dto.getUnit(),
                dto.getRole()
        ));

        create(notification, recipientUserIds);
        return recipientUserIds;
    }

    public void create(Notification notification, List<Long> recipientUserIds) {
        recipientUserIds.stream()
                .map(this::createRecipient)
                .forEach(notification::addRecipient);
    }

    private NotificationRecipient createRecipient(Long userId) {
        return NotificationRecipient.builder()
                .recipientUserId(userId)
                .build();
    }
}
