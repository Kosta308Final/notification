package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.*;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.event.NotificationCreatedEvent;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.resolver.NotificationTargetResolverComposite;
import com.alphatragen.notification.template.NotificationTemplateService;
import com.alphatragen.notification.template.TemplateResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationApplicationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationApplicationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;
    private final NotificationTemplateService templateService;
    private final NotificationTargetResolverComposite targetResolverComposite;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationApplicationService(
            NotificationRepository notificationRepository,
            NotificationSettingRepository settingRepository,
            NotificationTemplateService templateService,
            NotificationTargetResolverComposite targetResolverComposite,
            ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.settingRepository = settingRepository;
        this.templateService = templateService;
        this.targetResolverComposite = targetResolverComposite;
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
                    // 3. Template generation
                    Map<String, Object> templateData = new HashMap<>();
                    if (dto.getTemplateData() != null) {
                        templateData.putAll(dto.getTemplateData());
                    }
                    TemplateResult templateResult = templateService.generate(dto.getEventType(), templateData);

                    // 4. Calculate retention period
                    int retentionDays = settingRepository.findByApartmentId(dto.getApartmentId())
                            .map(NotificationSetting::getRetentionDays)
                            .orElse(90);
                    LocalDateTime createdAt = LocalDateTime.now();
                    LocalDateTime retentionUntil = createdAt.plusDays(retentionDays);

                    // 5. Create Notification origin
                    Notification notification = new Notification();
                    notification.setEventId(dto.getEventId());
                    notification.setImportance(dto.getEventType().getDefaultImportance());
                    notification.setSourceType(NotificationSourceType.DOMAIN);
                    notification.setTitle(templateResult.getTitle());
                    notification.setContent(templateResult.getContent());
                    notification.setActionUrl(dto.getActionUrl());
                    notification.setCreatedAt(createdAt);
                    notification.setRetentionUntil(retentionUntil);

                    // 6. Create Target condition
                    NotificationTarget target = new NotificationTarget();
                    target.setNotification(notification);
                    target.setTargetType(dto.getTargetType());
                    target.setApartmentId(dto.getApartmentId());
                    target.setUserId(dto.getUserId());
                    target.setBuilding(dto.getBuilding());
                    target.setUnit(dto.getUnit());
                    target.setRole(dto.getRole());
                    notification.getTargets().add(target);

                    // 7. Resolve recipients & Deduplication
                    List<Long> recipientUserIds = targetResolverComposite.resolveTargets(
                            dto.getTargetType(),
                            dto.getApartmentId(),
                            dto.getUserId(),
                            dto.getBuilding(),
                            dto.getUnit(),
                            dto.getRole()
                    );

                    // 8. Create Recipient records
                    List<NotificationRecipient> recipients = new ArrayList<>();
                    for (Long recipientUserId : recipientUserIds) {
                        NotificationRecipient recipient = new NotificationRecipient();
                        recipient.setNotification(notification);
                        recipient.setRecipientUserId(recipientUserId);
                        recipients.add(recipient);
                    }
                    notification.setRecipients(recipients);

                    // Save to DB (Single Transaction)
                    Notification saved = notificationRepository.save(notification);
                    log.info("notification_created eventId={} eventType={} apartmentId={} recipientCount={}",
                            dto.getEventId(), dto.getEventType(), dto.getApartmentId(), recipientUserIds.size());

                    // 9. Push Request (published to event listener running AFTER_COMMIT)
                    if (!recipientUserIds.isEmpty()) {
                        eventPublisher.publishEvent(new NotificationCreatedEvent(
                                saved.getId(),
                                recipientUserIds,
                                saved.getTitle(),
                                saved.getContent(),
                                saved.getActionUrl()
                        ));
                    }

                    return saved;
                });
    }
}
