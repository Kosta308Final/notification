package com.alphatragen.notification.service;

import com.alphatragen.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExpiredNotificationCleanupServiceTest {
    @Test
    void deletesExpiredNotificationsAndReturnsCount() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.deleteByRetentionUntilBefore(any())).thenReturn(3);

        int deleted = new ExpiredNotificationCleanupService(repository).deleteExpiredNotifications();

        assertEquals(3, deleted);
        verify(repository).deleteByRetentionUntilBefore(any());
    }
}
