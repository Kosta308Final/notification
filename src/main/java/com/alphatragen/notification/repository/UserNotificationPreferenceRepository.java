package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {
    Optional<UserNotificationPreference> findByUserIdAndApartmentId(Long userId, Long apartmentId);
}
