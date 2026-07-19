package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.*;
import com.alphatragen.notification.dto.ManualNotificationRequestDto;
import com.alphatragen.notification.dto.RecipientPreviewResponseDto;
import com.alphatragen.notification.dto.AdminNotificationResponseDto;
import com.alphatragen.notification.event.NotificationCreatedEvent;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.resolver.NotificationTargetResolverComposite;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationAdminService {
    private static final Logger log = LoggerFactory.getLogger(NotificationAdminService.class);
    private static final int DEFAULT_RETENTION_DAYS = 90;
    private final NotificationTargetResolverComposite resolver;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationAdminService(NotificationTargetResolverComposite resolver,
                                    NotificationRepository notificationRepository,
                                    NotificationSettingRepository settingRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.resolver = resolver;
        this.notificationRepository = notificationRepository;
        this.settingRepository = settingRepository;
        this.eventPublisher = eventPublisher;
    }

    public RecipientPreviewResponseDto preview(ManualNotificationRequestDto request, Long adminApartmentId, String roles) {
        authorize(request.getApartmentId(), adminApartmentId, roles);
        request.validateTargetSpecificFields();
        List<Long> ids = resolve(request);
        log.info("notification_recipient_preview apartmentId={} recipientCount={}", request.getApartmentId(), ids.size());
        return new RecipientPreviewResponseDto(ids.size(), ids);
    }

    @Transactional(readOnly = true)
    public Page<AdminNotificationResponseDto> getHistory(Long adminApartmentId, String roles, Pageable pageable) {
        authorize(adminApartmentId, adminApartmentId, roles);
        return notificationRepository.findAdminHistory(adminApartmentId, pageable)
                .map(AdminNotificationResponseDto::new);
    }

    @Transactional
    public Notification send(ManualNotificationRequestDto request, Long adminUserId, Long adminApartmentId, String roles) {
        authorize(request.getApartmentId(), adminApartmentId, roles);
        request.validateForSend();
        List<Long> ids = resolve(request);
        int retentionDays = request.getRetentionDays() != null ? request.getRetentionDays()
                : settingRepository.findByApartmentId(request.getApartmentId()).map(NotificationSetting::getRetentionDays).orElse(DEFAULT_RETENTION_DAYS);

        Notification notification = new Notification();
        notification.setEventId("MANUAL-" + UUID.randomUUID());
        notification.setImportance(request.getImportance());
        notification.setSourceType(NotificationSourceType.OFFICE_MANUAL);
        notification.setCreatedBy(adminUserId);
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setActionUrl(request.getActionUrl());
        LocalDateTime createdAt = LocalDateTime.now();
        notification.setCreatedAt(createdAt);
        notification.setRetentionUntil(createdAt.plusDays(retentionDays));

        NotificationTarget target = new NotificationTarget();
        target.setNotification(notification);
        target.setTargetType(request.getTargetType());
        target.setApartmentId(request.getApartmentId());
        target.setUserId(request.getUserId());
        target.setBuilding(request.getBuilding());
        target.setUnit(request.getUnit());
        target.setRole(request.getRole());
        notification.getTargets().add(target);

        List<NotificationRecipient> recipients = new ArrayList<>();
        ids.forEach(id -> { NotificationRecipient recipient = new NotificationRecipient(); recipient.setNotification(notification); recipient.setRecipientUserId(id); recipients.add(recipient); });
        notification.setRecipients(recipients);
        Notification saved = notificationRepository.save(notification);
        log.info("notification_manual_created eventId={} apartmentId={} adminUserId={} recipientCount={}",
                saved.getEventId(), request.getApartmentId(), adminUserId, ids.size());
        if (!ids.isEmpty()) eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getId(), ids, saved.getTitle(), saved.getContent(), saved.getActionUrl()));
        return saved;
    }

    private List<Long> resolve(ManualNotificationRequestDto request) {
        return resolver.resolveTargets(request.getTargetType(), request.getApartmentId(), request.getUserId(), request.getBuilding(), request.getUnit(), request.getRole());
    }

    private void authorize(Long requestedApartmentId, Long adminApartmentId, String roles) {
        if (adminApartmentId == null || !requestedApartmentId.equals(adminApartmentId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "apartment access denied");
        if (roles == null || !(roles.toUpperCase().contains("ADMIN") || roles.toUpperCase().contains("MANAGER") || roles.toUpperCase().contains("OFFICE"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin role required");
    }
}
