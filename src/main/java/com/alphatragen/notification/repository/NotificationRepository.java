package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
    int deleteByRetentionUntilBefore(LocalDateTime cutoff);

    @Query("select distinct n from Notification n join n.targets t " +
            "where t.apartmentId = :apartmentId order by n.createdAt desc")
    Page<Notification> findAdminHistory(Long apartmentId, Pageable pageable);
}
