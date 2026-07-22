package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.event.NotificationCreatedEvent;
import com.alphatragen.notification.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class NotificationApplicationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationRecipientCreator notificationRecipientCreator;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationApplicationService(
            NotificationRepository notificationRepository,
            NotificationFactory notificationFactory,
            NotificationRecipientCreator notificationRecipientCreator,
            ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.notificationFactory = notificationFactory;
        this.notificationRecipientCreator = notificationRecipientCreator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Notification createNotification(NotificationEventDto dto) {
        log.info("notification_create_started eventId={} eventType={} apartmentId={}",
                dto == null ? null : dto.getEventId(), dto == null ? null : dto.getEventType(), dto == null ? null : dto.getApartmentId());
        if (dto == null) {
            throw new IllegalArgumentException("notification event is required");
        }
        // 1. Event validation
        if (dto.getEventId() == null || dto.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        if (dto.getEventType() == null) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (dto.getApartmentId() == null) {
            throw new IllegalArgumentException("apartmentId is required");
        }
        if (dto.getTargetType() == null) {
            throw new IllegalArgumentException("targetType is required");
        }
        dto.validateTargetSpecificFields();

        // 2. Existing eventId check (Idempotency)
        return notificationRepository.findByEventId(dto.getEventId())
                .map(existing -> {
                    log.info("notification_duplicate eventId={} eventType={} apartmentId={}", dto.getEventId(), dto.getEventType(), dto.getApartmentId());
                    return existing;
                })
                .orElseGet(() -> {
                    Notification notification = notificationFactory.create(dto);
                    List<Long> recipientUserIds = notificationRecipientCreator.create(notification, dto);

                    // Save to DB (Single Transaction)
                    Notification saved = notificationRepository.save(notification);
                    log.info("notification_created eventId={} eventType={} apartmentId={} recipientCount={}",
                            dto.getEventId(), dto.getEventType(), dto.getApartmentId(), recipientUserIds.size());

                    // 9. Push Request (published to event listener running AFTER_COMMIT)
                    if (!recipientUserIds.isEmpty()) {
                        eventPublisher.publishEvent(new NotificationCreatedEvent(
                                saved.getId(),
                                recipientUserIds,
                                saved.getTargets().get(0).getApartmentId(),
                                saved.getTitle(),
                                saved.getContent(),
                                saved.getActionUrl(),
                                saved.getEventId(),
                                saved.getImportance(),
                                saved.getCreatedAt()
                        ));
                    }

                    return saved;
                });
    }
}
