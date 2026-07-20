package com.alphatragen.notification.service;

import com.alphatragen.notification.domain.NotificationSetting;
import com.alphatragen.notification.repository.NotificationSettingRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationSettingServiceTest {
    private final NotificationSettingRepository repository = mock(NotificationSettingRepository.class);
    private final NotificationSettingService service = new NotificationSettingService(repository);

    @Test
    void returnsDefaultWhenApartmentHasNoSetting() {
        when(repository.findByApartmentId(10L)).thenReturn(Optional.empty());

        var result = service.get(10L);

        assertEquals(10L, result.apartmentId());
        assertEquals(90, result.retentionDays());
        verify(repository, never()).save(any());
    }

    @Test
    void updatesSettingAndRecordsAdministrator() {
        when(repository.findByApartmentId(10L)).thenReturn(Optional.empty());
        when(repository.save(any(NotificationSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.update(10L, 180, 99L, 10L, "OFFICE_ADMIN");

        assertEquals(180, result.retentionDays());
        assertEquals(99L, result.updatedBy());
        verify(repository).save(any(NotificationSetting.class));
    }

    @Test
    void rejectsOutOfRangeAndUnauthorizedUpdates() {
        assertThrows(IllegalArgumentException.class, () -> service.update(10L, 29, 99L, 10L, "ADMIN"));
        assertThrows(IllegalArgumentException.class, () -> service.update(10L, 366, 99L, 10L, "ADMIN"));
        assertThrows(Exception.class, () -> service.update(10L, 90, 99L, 20L, "ADMIN"));
        verifyNoInteractions(repository);
    }
}
