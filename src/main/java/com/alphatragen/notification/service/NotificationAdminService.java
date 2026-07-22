package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationSetting;
import com.alphatragen.notification.dto.ManualNotificationReqDto;
import com.alphatragen.notification.dto.RecipientPreviewRespDto;
import com.alphatragen.notification.dto.AdminNotificationRespDto;
import com.alphatragen.notification.event.NotificationCreatedEvent;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.resolver.NotificationTargetResolverComposite;
import com.alphatragen.notification.resolver.TargetCondition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class NotificationAdminService {
    private static final Logger log = LoggerFactory.getLogger(NotificationAdminService.class);
    private static final int DEFAULT_RETENTION_DAYS = 90;
    private final NotificationTargetResolverComposite resolver;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationRecipientCreator notificationRecipientCreator;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationAdminService(NotificationTargetResolverComposite resolver,
                                    NotificationRepository notificationRepository,
                                    NotificationSettingRepository settingRepository,
                                    NotificationFactory notificationFactory,
                                    NotificationRecipientCreator notificationRecipientCreator,
                                    ApplicationEventPublisher eventPublisher) {
        this.resolver = resolver;
        this.notificationRepository = notificationRepository;
        this.settingRepository = settingRepository;
        this.notificationFactory = notificationFactory;
        this.notificationRecipientCreator = notificationRecipientCreator;
        this.eventPublisher = eventPublisher;
    }

    public RecipientPreviewRespDto preview(ManualNotificationReqDto request, Long adminApartmentId, String roles) {
        authorize(request.apartmentId(), adminApartmentId, roles);
        request.validateTargetSpecificFields();
        List<Long> ids = resolve(request);
        log.info("notification_recipient_preview apartmentId={} recipientCount={}", request.apartmentId(), ids.size());
        return new RecipientPreviewRespDto(ids.size(), ids);
    }

    @Transactional(readOnly = true)
    public Page<AdminNotificationRespDto> getHistory(Long adminApartmentId, String roles, Pageable pageable) {
        authorize(adminApartmentId, adminApartmentId, roles);
        return notificationRepository.findAdminHistory(adminApartmentId, pageable)
                .map(AdminNotificationRespDto::new);
    }

    @Transactional
    public Notification send(ManualNotificationReqDto request, Long adminUserId, Long adminApartmentId, String roles) {
        authorize(request.apartmentId(), adminApartmentId, roles);
        request.validateForSend();
        List<Long> ids = resolve(request);
        int retentionDays = request.retentionDays() != null ? request.retentionDays()
                : settingRepository.findByApartmentId(request.apartmentId()).map(NotificationSetting::getRetentionDays).orElse(DEFAULT_RETENTION_DAYS);

        Notification notification = notificationFactory.createManual(request, adminUserId, retentionDays);
        notificationRecipientCreator.create(notification, ids);
        Notification saved = notificationRepository.save(notification);
        log.info("notification_manual_created eventId={} apartmentId={} adminUserId={} recipientCount={}",
                saved.getEventId(), request.apartmentId(), adminUserId, ids.size());
        if (!ids.isEmpty()) eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getId(), ids, saved.getTitle(), saved.getContent(), saved.getActionUrl()));
        return saved;
    }

    private List<Long> resolve(ManualNotificationReqDto request) {
        return resolver.resolveTargets(new TargetCondition(
                request.targetType(),
                request.apartmentId(),
                request.userId(),
                request.building(),
                request.unit(),
                request.role()
        ));
    }

    private void authorize(Long requestedApartmentId, Long adminApartmentId, String roles) {
        if (adminApartmentId == null || !requestedApartmentId.equals(adminApartmentId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "apartment access denied");
        if (roles == null || !(roles.toUpperCase().contains("ADMIN") || roles.toUpperCase().contains("MANAGER") || roles.toUpperCase().contains("OFFICE"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin role required");
    }
}
