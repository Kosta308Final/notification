package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationSetting;
import com.alphatragen.notification.domain.NotificationSourceType;
import com.alphatragen.notification.domain.NotificationTarget;
import com.alphatragen.notification.dto.NotificationEventDto;
import com.alphatragen.notification.dto.ManualNotificationReqDto;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.template.NotificationTemplateService;
import com.alphatragen.notification.template.TemplateResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationFactory {

    private static final int DEFAULT_RETENTION_DAYS = 90;

    private final NotificationSettingRepository settingRepository;
    private final NotificationTemplateService templateService;

    public NotificationFactory(
            NotificationSettingRepository settingRepository,
            NotificationTemplateService templateService) {
        this.settingRepository = settingRepository;
        this.templateService = templateService;
    }

    public Notification create(NotificationEventDto dto) {
        Map<String, Object> templateData = new HashMap<>();
        if (dto.getTemplateData() != null) {
            templateData.putAll(dto.getTemplateData());
        }
        TemplateResult templateResult = templateService.generate(dto.getEventType(), templateData);

        LocalDateTime createdAt = LocalDateTime.now();
        int retentionDays = settingRepository.findByApartmentIdAndUserIdIsNull(dto.getApartmentId())
                .map(NotificationSetting::getRetentionDays)
                .orElse(DEFAULT_RETENTION_DAYS);

        Notification notification = Notification.builder()
                .eventId(dto.getEventId())
                .importance(dto.getEventType().getDefaultImportance())
                .sourceType(NotificationSourceType.DOMAIN)
                .title(templateResult.getTitle())
                .content(templateResult.getContent())
                .actionUrl(dto.getActionUrl())
                .createdAt(createdAt)
                .retentionUntil(createdAt.plusDays(retentionDays))
                .build();

        NotificationTarget target = NotificationTarget.builder()
                .targetType(dto.getTargetType())
                .apartmentId(dto.getApartmentId())
                .userId(dto.getUserId())
                .building(dto.getBuilding())
                .unit(dto.getUnit())
                .role(dto.getRole())
                .build();
        notification.addTarget(target);

        return notification;
    }

    public Notification createManual(ManualNotificationReqDto request, Long adminUserId, int retentionDays) {
        LocalDateTime createdAt = LocalDateTime.now();

        Notification notification = Notification.builder()
                .eventId("MANUAL-" + java.util.UUID.randomUUID())
                .importance(request.importance())
                .sourceType(NotificationSourceType.OFFICE_MANUAL)
                .createdBy(adminUserId)
                .title(request.title())
                .content(request.content())
                .actionUrl(request.actionUrl())
                .createdAt(createdAt)
                .retentionUntil(createdAt.plusDays(retentionDays))
                .build();

        NotificationTarget target = NotificationTarget.builder()
                .targetType(request.targetType())
                .apartmentId(request.apartmentId())
                .userId(request.userId())
                .building(request.building())
                .unit(request.unit())
                .role(request.role())
                .build();
        notification.addTarget(target);

        return notification;
    }
}
