package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.NotificationRecipient;
import com.alphatragen.notification.repository.NotificationRecipientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class NotificationUserService {

    private final NotificationRecipientRepository recipientRepository;

    public NotificationUserService(NotificationRecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    public Page<NotificationRecipient> getNotifications(Long userId, Long apartmentId, Boolean isRead, Pageable pageable) {
        return recipientRepository.findNotifications(userId, apartmentId, isRead, LocalDateTime.now(), pageable);
    }

    public long getUnreadCount(Long userId, Long apartmentId) {
        return recipientRepository.countUnreadNotifications(userId, apartmentId, LocalDateTime.now());
    }

    @Transactional
    public NotificationRecipient markAsRead(Long id, Long userId) {
        NotificationRecipient recipient = recipientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!recipient.getRecipientUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this notification");
        }

        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadAt(LocalDateTime.now());
            return recipientRepository.save(recipient);
        }
        return recipient;
    }

    @Transactional
    public NotificationRecipient markAsRead(Long id, Long userId, Long apartmentId) {
        NotificationRecipient recipient = recipientRepository.findByIdAndRecipientUserIdAndApartmentId(id, userId, apartmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadAt(LocalDateTime.now());
            return recipientRepository.save(recipient);
        }
        return recipient;
    }

    @Transactional
    public void markAllAsRead(Long userId, Long apartmentId) {
        recipientRepository.markAllAsRead(userId, apartmentId, LocalDateTime.now());
    }
}
