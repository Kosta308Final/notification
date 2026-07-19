package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationTargetType;
import com.alphatragen.notification.dto.ManualNotificationRequestDto;
import com.alphatragen.notification.dto.RecipientPreviewResponseDto;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.resolver.NotificationTargetResolverComposite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationAdminServiceTest {
    private NotificationTargetResolverComposite resolver;
    private NotificationRepository notificationRepository;
    private NotificationSettingRepository settingRepository;
    private ApplicationEventPublisher eventPublisher;
    private NotificationAdminService service;

    @BeforeEach
    void setUp() {
        resolver = mock(NotificationTargetResolverComposite.class);
        notificationRepository = mock(NotificationRepository.class);
        settingRepository = mock(NotificationSettingRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new NotificationAdminService(resolver, notificationRepository, settingRepository, eventPublisher);
    }

    @Test
    void previewDeduplicatesAndReturnsRecipientCount() {
        ManualNotificationRequestDto request = request(NotificationTargetType.APARTMENT);
        when(resolver.resolveTargets(any(), eq(1L), isNull(), isNull(), isNull(), isNull())).thenReturn(List.of(10L, 20L));

        RecipientPreviewResponseDto result = service.preview(request, 1L, "OFFICE_ADMIN");

        assertEquals(2, result.count());
        assertEquals(List.of(10L, 20L), result.userIds());
    }

    @Test
    void sendStoresManualNotificationAndPublishesPushEvent() {
        ManualNotificationRequestDto request = request(NotificationTargetType.INDIVIDUAL);
        request.setUserId(10L);
        request.setTitle("점검 안내");
        request.setContent("내일 점검합니다.");
        request.setImportance(NotificationImportance.URGENT);
        when(resolver.resolveTargets(any(), any(), any(), any(), any(), any())).thenReturn(List.of(10L));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification saved = service.send(request, 99L, 1L, "ADMIN");

        assertEquals(NotificationImportance.URGENT, saved.getImportance());
        assertEquals("OFFICE_MANUAL", saved.getSourceType().name());
        assertEquals(99L, saved.getCreatedBy());
        assertEquals(1, saved.getRecipients().size());
    }

    @Test
    void normalUserAndOtherApartmentAreRejected() {
        ManualNotificationRequestDto request = request(NotificationTargetType.APARTMENT);
        assertThrows(ResponseStatusException.class, () -> service.preview(request, 1L, "USER"));
        assertThrows(ResponseStatusException.class, () -> service.preview(request, 2L, "ADMIN"));
        verifyNoInteractions(resolver);
    }

    private ManualNotificationRequestDto request(NotificationTargetType type) {
        ManualNotificationRequestDto request = new ManualNotificationRequestDto();
        request.setApartmentId(1L);
        request.setTargetType(type);
        return request;
    }
}
