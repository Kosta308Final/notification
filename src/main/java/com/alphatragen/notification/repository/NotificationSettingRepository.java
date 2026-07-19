package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByApartmentId(Long apartmentId);
}
