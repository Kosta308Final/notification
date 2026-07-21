package com.alphatragen.notification.controller;

import com.alphatragen.notification.domain.*;
import com.alphatragen.notification.dto.NotificationRespDto;
import com.alphatragen.notification.service.NotificationUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private NotificationUserService userService;
    private InternalNotificationController controller;

    @BeforeEach
    void setUp() {
        userService = mock(NotificationUserService.class);
        controller = new InternalNotificationController(userService);
    }

    @Test
    void testGetNotifications() {
        // Given
        Long userId = 100L;
        Long apartmentId = 10L;
        Pageable pageable = PageRequest.of(0, 20);

        Notification n = Notification.builder()
                .title("Title")
                .content("Content")
                .importance(NotificationImportance.NORMAL)
                .createdAt(LocalDateTime.now())
                .actionUrl("/path")
                .build();

        NotificationRecipient r = NotificationRecipient.builder()
                .id(1L)
                .notification(n)
                .recipientUserId(userId)
                .read(false)
                .build();

        Page<NotificationRecipient> page = new PageImpl<>(List.of(r), pageable, 1);
        when(userService.getNotifications(userId, apartmentId, null, pageable)).thenReturn(page);

        // When
        Page<NotificationRespDto> result = controller.getNotifications(userId, apartmentId, null, 0, 20);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Title", result.getContent().get(0).getTitle());
        assertEquals("/path", result.getContent().get(0).getActionUrl());
        assertFalse(result.getContent().get(0).isRead());
    }

    @Test
    void testGetUnreadCount() {
        // Given
        Long userId = 100L;
        Long apartmentId = 10L;
        when(userService.getUnreadCount(userId, apartmentId)).thenReturn(5L);

        // When
        long count = controller.getUnreadCount(userId, apartmentId);

        // Then
        assertEquals(5L, count);
    }

    @Test
    void testMarkAsRead() {
        // Given
        Long notificationId = 1L;
        Long userId = 100L;

        // When
        controller.markAsRead(notificationId, userId);

        // Then
        verify(userService, times(1)).markAsRead(notificationId, userId);
    }

    @Test
    void testMarkAllAsRead() {
        // Given
        Long userId = 100L;
        Long apartmentId = 10L;

        // When
        controller.markAllAsRead(userId, apartmentId);

        // Then
        verify(userService, times(1)).markAllAsRead(userId, apartmentId);
    }
}
