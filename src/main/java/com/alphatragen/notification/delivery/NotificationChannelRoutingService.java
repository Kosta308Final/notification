package com.alphatragen.notification.delivery;

import com.alphatragen.notification.domain.PcChannelMode;
import com.alphatragen.notification.event.NotificationCreatedEvent;
import com.alphatragen.notification.push.PushSender;
import com.alphatragen.notification.realtime.RealtimeConnectionService;
import com.alphatragen.notification.repository.DesktopDeviceRepository;
import com.alphatragen.notification.repository.PushSubscriptionRepository;
import com.alphatragen.notification.repository.UserNotificationPreferenceRepository;
import com.alphatragen.notification.domain.UserNotificationPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationChannelRoutingService {
    private static final Logger log = LoggerFactory.getLogger(NotificationChannelRoutingService.class);
    private final UserNotificationPreferenceRepository preferences;
    private final DesktopDeviceRepository devices;
    private final PushSubscriptionRepository subscriptions;
    private final PushSender pushSender;
    private final RealtimeConnectionService realtime;

    public NotificationChannelRoutingService(UserNotificationPreferenceRepository preferences, DesktopDeviceRepository devices, PushSubscriptionRepository subscriptions, PushSender pushSender, RealtimeConnectionService realtime) {
        this.preferences = preferences; this.devices = devices; this.subscriptions = subscriptions; this.pushSender = pushSender; this.realtime = realtime;
    }

    public void deliver(NotificationCreatedEvent event) {
        for (Long userId : event.recipientUserIds()) {
            UserNotificationPreference preference = preferences.findByUserIdAndApartmentId(userId, event.apartmentId()).orElse(null);
            PcChannelMode mode = preference == null ? PcChannelMode.DESKTOP_FIRST : preference.getPcChannelMode();
            boolean desktop = !devices.findByUserIdAndApartmentIdAndActiveTrue(userId, event.apartmentId()).isEmpty();
            boolean webPush = !subscriptions.findByUserIdAndApartmentIdAndIsActiveTrue(userId, event.apartmentId()).isEmpty();
            if (mode == PcChannelMode.DISABLED) { log.info("notification_channel_result notificationId={} userId={} result=SKIPPED_BY_POLICY", event.notificationId(), userId); continue; }
            if (mode == PcChannelMode.DESKTOP_FIRST && desktop) { sendDesktop(event, userId); continue; }
            if (mode == PcChannelMode.WEB_PUSH_FIRST && webPush) { sendPush(event, userId); continue; }
            if (mode == PcChannelMode.BOTH) { sendDesktop(event, userId); sendPush(event, userId); continue; }
            sendPush(event, userId);
        }
    }

    private void sendDesktop(NotificationCreatedEvent event, Long userId) {
        realtime.send(new NotificationCreatedEvent(event.notificationId(), List.of(userId), event.apartmentId(), event.title(), event.content(), event.actionUrl(), event.eventId(), event.importance(), event.createdAt()));
    }
    private void sendPush(NotificationCreatedEvent event, Long userId) {
        try { pushSender.sendPush(event.notificationId(), userId, event.apartmentId(), event.title(), event.content(), event.actionUrl()); }
        catch (Exception e) { log.warn("notification_channel_fallback notificationId={} userId={} channel=WEB_PUSH", event.notificationId(), userId, e); }
    }
}
