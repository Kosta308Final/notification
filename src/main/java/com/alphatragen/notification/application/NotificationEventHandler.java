package com.alphatragen.notification.application;

import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.service.NotificationApplicationService;
import com.alphatragen.notification.service.PushSubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventHandler {

    private final NotificationApplicationService notificationApplicationService;
    private final PushSubscriptionService pushSubscriptionService;

    public NotificationEventHandler(
            NotificationApplicationService notificationApplicationService,
            PushSubscriptionService pushSubscriptionService) {
        this.notificationApplicationService = notificationApplicationService;
        this.pushSubscriptionService = pushSubscriptionService;
    }

    public void handle(NotificationEventDto eventDto) {
        if (eventDto.getEventType() == NotificationEventType.USER_WITHDRAWN) {
            pushSubscriptionService.deactivateSubscriptionByWithdrawal(eventDto.getUserId());
            return;
        }

        notificationApplicationService.createNotification(eventDto);
    }
}
