package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    List<NotificationRecipient> findByRecipientUserId(Long recipientUserId);
    boolean existsByNotificationIdAndRecipientUserId(Long notificationId, Long recipientUserId);

    @Query("SELECT r FROM NotificationRecipient r JOIN r.notification n " +
           "WHERE r.id = :id AND r.recipientUserId = :userId " +
           "AND EXISTS (SELECT 1 FROM NotificationTarget t WHERE t.notification = n AND t.apartmentId = :apartmentId)")
    java.util.Optional<NotificationRecipient> findByIdAndRecipientUserIdAndApartmentId(
            @Param("id") Long id, @Param("userId") Long userId, @Param("apartmentId") Long apartmentId);

    @Query("SELECT r FROM NotificationRecipient r " +
           "JOIN r.notification n " +
           "WHERE r.recipientUserId = :recipientUserId " +
           "AND EXISTS (SELECT 1 FROM NotificationTarget t WHERE t.notification = n AND t.apartmentId = :apartmentId) " +
           "AND (:isRead IS NULL OR r.isRead = :isRead) " +
           "AND n.retentionUntil > :now " +
           "ORDER BY CASE WHEN (r.isRead = false AND n.importance = com.alphatragen.notification.domain.NotificationImportance.URGENT) THEN 0 ELSE 1 END ASC, n.createdAt DESC")
    Page<NotificationRecipient> findNotifications(
            @Param("recipientUserId") Long recipientUserId,
            @Param("apartmentId") Long apartmentId,
            @Param("isRead") Boolean isRead,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT r FROM NotificationRecipient r JOIN r.notification n " +
           "WHERE r.recipientUserId = :recipientUserId " +
           "AND n.id > :afterNotificationId " +
           "AND EXISTS (SELECT 1 FROM NotificationTarget t WHERE t.notification = n AND t.apartmentId = :apartmentId) " +
           "AND n.retentionUntil > :now ORDER BY n.id ASC")
    Page<NotificationRecipient> findNotificationsAfter(
            @Param("recipientUserId") Long recipientUserId,
            @Param("apartmentId") Long apartmentId,
            @Param("afterNotificationId") Long afterNotificationId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT COUNT(r) FROM NotificationRecipient r " +
           "JOIN r.notification n " +
           "WHERE r.recipientUserId = :recipientUserId " +
           "AND EXISTS (SELECT 1 FROM NotificationTarget t WHERE t.notification = n AND t.apartmentId = :apartmentId) " +
           "AND r.isRead = false " +
           "AND n.retentionUntil > :now")
    long countUnreadNotifications(
            @Param("recipientUserId") Long recipientUserId,
            @Param("apartmentId") Long apartmentId,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationRecipient r SET r.isRead = true, r.readAt = :now " +
           "WHERE r.recipientUserId = :recipientUserId " +
           "AND r.isRead = false " +
           "AND EXISTS (SELECT 1 FROM NotificationTarget t WHERE t.notification = r.notification AND t.apartmentId = :apartmentId)")
    int markAllAsRead(
            @Param("recipientUserId") Long recipientUserId,
            @Param("apartmentId") Long apartmentId,
            @Param("now") LocalDateTime now
    );
}
