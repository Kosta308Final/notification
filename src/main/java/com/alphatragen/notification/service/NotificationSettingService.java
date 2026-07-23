package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.NotificationSetting;
import com.alphatragen.notification.dto.NotificationSettingRespDto;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import com.alphatragen.notification.domain.PcChannelMode;
import com.alphatragen.notification.domain.UserNotificationPreference;
import com.alphatragen.notification.repository.UserNotificationPreferenceRepository;

@Service
public class NotificationSettingService {
    public static final int DEFAULT_RETENTION_DAYS = 90;
    private final NotificationSettingRepository repository;
    private final UserNotificationPreferenceRepository preferenceRepository;

    @Autowired
    public NotificationSettingService(NotificationSettingRepository repository, UserNotificationPreferenceRepository preferenceRepository) {
        this.repository = repository; this.preferenceRepository = preferenceRepository;
    }

    public NotificationSettingService(NotificationSettingRepository repository) {
        this.repository = repository; this.preferenceRepository = null;
    }

    @Transactional(readOnly = true)
    public NotificationSettingRespDto get(Long apartmentId) {
        if (apartmentId == null) throw new IllegalArgumentException("apartmentId is required");
        NotificationSetting setting = repository.findByApartmentId(apartmentId).orElseGet(() -> {
            return NotificationSetting.builder()
                    .apartmentId(apartmentId)
                    .retentionDays(DEFAULT_RETENTION_DAYS)
                    .build();
        });
        return NotificationSettingRespDto.from(setting);
    }

    @Transactional(readOnly = true)
    public NotificationSettingRespDto getForUser(Long userId, Long apartmentId) {
        if (preferenceRepository == null) return get(apartmentId);
        UserNotificationPreference preference = preferenceRepository.findByUserIdAndApartmentId(userId, apartmentId).orElseGet(() -> new UserNotificationPreference(userId, apartmentId));
        return new NotificationSettingRespDto(apartmentId, DEFAULT_RETENTION_DAYS, userId, preference.getUpdatedAt(), preference.getPcChannelMode(), preference.isDesktopNativeEnabled(), preference.isFloatingEnabled(), preference.isUrgentAutoExpand());
    }

    @Transactional
    public NotificationSettingRespDto updateForUser(Long userId, Long apartmentId, com.alphatragen.notification.dto.NotificationSettingUpdateReqDto request) {
        if (preferenceRepository == null) return update(apartmentId, request.retentionDays() == null ? DEFAULT_RETENTION_DAYS : request.retentionDays(), userId, apartmentId, "USER");
        UserNotificationPreference preference = preferenceRepository.findByUserIdAndApartmentId(userId, apartmentId).orElseGet(() -> new UserNotificationPreference(userId, apartmentId));
        preference.update(request.pcChannelMode() == null ? PcChannelMode.DESKTOP_FIRST : request.pcChannelMode(),
                request.desktopNativeEnabled() == null || request.desktopNativeEnabled(),
                request.floatingEnabled() == null || request.floatingEnabled(),
                request.urgentAutoExpand() == null || request.urgentAutoExpand());
        UserNotificationPreference saved = preferenceRepository.save(preference);
        return new NotificationSettingRespDto(apartmentId, DEFAULT_RETENTION_DAYS, userId, saved.getUpdatedAt(), saved.getPcChannelMode(), saved.isDesktopNativeEnabled(), saved.isFloatingEnabled(), saved.isUrgentAutoExpand());
    }

    @Transactional
    public NotificationSettingRespDto update(Long apartmentId, int retentionDays, Long adminUserId,
                                             Long adminApartmentId, String roles) {
        authorize(apartmentId, adminApartmentId, roles);
        if (retentionDays < 30 || retentionDays > 365) {
            throw new IllegalArgumentException("retentionDays must be between 30 and 365 days");
        }
        NotificationSetting setting = repository.findByApartmentId(apartmentId).orElseGet(() -> {
            return NotificationSetting.builder()
                    .apartmentId(apartmentId)
                    .build();
        });
        setting.updateRetention(retentionDays, adminUserId, LocalDateTime.now());
        return NotificationSettingRespDto.from(repository.save(setting));
    }

    private void authorize(Long requestedApartmentId, Long adminApartmentId, String roles) {
        if (requestedApartmentId == null || !requestedApartmentId.equals(adminApartmentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "apartment access denied");
        }
        String normalized = roles == null ? "" : roles.toUpperCase();
        if (!(normalized.contains("ADMIN") || normalized.contains("MANAGER") || normalized.contains("OFFICE"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin role required");
        }
    }
}
