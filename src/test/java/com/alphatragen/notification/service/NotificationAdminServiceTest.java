package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.Notification;
import com.alphatragen.notification.domain.NotificationImportance;
import com.alphatragen.notification.domain.NotificationTargetType;
import com.alphatragen.notification.dto.ManualNotificationReqDto;
import com.alphatragen.notification.dto.RecipientPreviewRespDto;
import com.alphatragen.notification.repository.NotificationRepository;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import com.alphatragen.notification.resolver.NotificationTargetResolverComposite;
import com.alphatragen.notification.resolver.TargetCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationAdminServiceTest {
    private NotificationTargetResolverComposite resolver;
    private NotificationRepository notificationRepository;
    private NotificationSettingRepository settingRepository;
    private NotificationFactory notificationFactory;
    private NotificationRecipientCreator notificationRecipientCreator;
    private ApplicationEventPublisher eventPublisher;
    private NotificationAdminService service;

    @BeforeEach
    void setUp() {
        resolver = mock(NotificationTargetResolverComposite.class);
        notificationRepository = mock(NotificationRepository.class);
        settingRepository = mock(NotificationSettingRepository.class);
        notificationFactory = new NotificationFactory(settingRepository, mock(com.alphatragen.notification.template.NotificationTemplateService.class));
        notificationRecipientCreator = new NotificationRecipientCreator(resolver);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new NotificationAdminService(resolver, notificationRepository, settingRepository,
                notificationFactory, notificationRecipientCreator, eventPublisher);
    }

    @Test
    void previewDeduplicatesAndReturnsRecipientCount() {
        ManualNotificationReqDto request = request(NotificationTargetType.APARTMENT);
        when(resolver.resolveTargets(any(TargetCondition.class))).thenReturn(List.of(10L, 20L));

        RecipientPreviewRespDto result = service.preview(request, 1L, "OFFICE_ADMIN");

        assertEquals(2, result.count());
        assertEquals(List.of(10L, 20L), result.userIds());
    }

    @Test
    void sendStoresManualNotificationAndPublishesPushEvent() {
        ManualNotificationReqDto request = new ManualNotificationReqDto(1L, NotificationTargetType.INDIVIDUAL, 10L,
                null, null, null, NotificationImportance.URGENT, "title", "content", null, null);
        when(resolver.resolveTargets(any(TargetCondition.class))).thenReturn(List.of(10L));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification saved = service.send(request, 99L, 1L, "ADMIN");

        assertEquals(NotificationImportance.URGENT, saved.getImportance());
        assertEquals("OFFICE_MANUAL", saved.getSourceType().name());
        assertEquals(99L, saved.getCreatedBy());
        assertEquals(1, saved.getRecipients().size());
    }

    @Test
    void normalUserAndOtherApartmentAreRejected() {
        ManualNotificationReqDto request = request(NotificationTargetType.APARTMENT);
        assertThrows(ResponseStatusException.class, () -> service.preview(request, 1L, "USER"));
        assertThrows(ResponseStatusException.class, () -> service.preview(request, 2L, "ADMIN"));
        verifyNoInteractions(resolver);
    }

    private ManualNotificationReqDto request(NotificationTargetType type) {
        return new ManualNotificationReqDto(1L, type, null, null, null, null,
                NotificationImportance.NORMAL, null, null, null, null);
    }
}
