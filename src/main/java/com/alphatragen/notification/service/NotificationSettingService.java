package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.NotificationSetting;
import com.alphatragen.notification.dto.NotificationSettingResponseDto;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class NotificationSettingService {
    public static final int DEFAULT_RETENTION_DAYS = 90;
    private final NotificationSettingRepository repository;

    public NotificationSettingService(NotificationSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public NotificationSettingResponseDto get(Long apartmentId) {
        if (apartmentId == null) throw new IllegalArgumentException("apartmentId is required");
        NotificationSetting setting = repository.findByApartmentId(apartmentId).orElseGet(() -> {
            NotificationSetting defaultSetting = new NotificationSetting();
            defaultSetting.setApartmentId(apartmentId);
            defaultSetting.setRetentionDays(DEFAULT_RETENTION_DAYS);
            return defaultSetting;
        });
        return NotificationSettingResponseDto.from(setting);
    }

    @Transactional
    public NotificationSettingResponseDto update(Long apartmentId, int retentionDays, Long adminUserId,
                                                  Long adminApartmentId, String roles) {
        authorize(apartmentId, adminApartmentId, roles);
        if (retentionDays < 30 || retentionDays > 365) {
            throw new IllegalArgumentException("retentionDays must be between 30 and 365 days");
        }
        NotificationSetting setting = repository.findByApartmentId(apartmentId).orElseGet(() -> {
            NotificationSetting created = new NotificationSetting();
            created.setApartmentId(apartmentId);
            return created;
        });
        setting.setRetentionDays(retentionDays);
        setting.setUpdatedBy(adminUserId);
        setting.setUpdatedAt(LocalDateTime.now());
        return NotificationSettingResponseDto.from(repository.save(setting));
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
