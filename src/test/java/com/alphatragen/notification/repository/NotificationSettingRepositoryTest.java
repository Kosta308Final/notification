package com.alphatragen.notification.repository;

import com.alphatragen.notification.config.JpaConfig;
import com.alphatragen.notification.domain.NotificationSetting;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Tag("jpa")
@Import(JpaConfig.class)
class NotificationSettingRepositoryTest {

    @Autowired
    private NotificationSettingRepository settingRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveSettingSuccess() {
        NotificationSetting setting = NotificationSetting.builder()
                .apartmentId(101L)
                .retentionDays(120)
                .build();

        NotificationSetting saved = settingRepository.save(setting);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals(101L, saved.getApartmentId());
        assertEquals(120, saved.getRetentionDays());
    }

    @Test
    void testDefaultRetentionDaysIs90() {
        NotificationSetting setting = NotificationSetting.builder()
                .apartmentId(102L)
                .build();

        NotificationSetting saved = settingRepository.save(setting);
        entityManager.flush();

        assertEquals(90, saved.getRetentionDays());
    }

    @Test
    void testRetentionDaysOutOfRangeThrowsException() {
        NotificationSetting setting = NotificationSetting.builder()
                .apartmentId(103L)
                .build();

        // Less than 30
        assertThrows(IllegalArgumentException.class, () -> setting.updateRetention(29, 1L, java.time.LocalDateTime.now()));

        // Greater than 365
        assertThrows(IllegalArgumentException.class, () -> setting.updateRetention(366, 1L, java.time.LocalDateTime.now()));
    }

    @Test
    void testDuplicateApartmentIdThrowsException() {
        NotificationSetting setting1 = NotificationSetting.builder()
                .apartmentId(104L)
                .build();
        settingRepository.save(setting1);
        entityManager.flush();

        NotificationSetting setting2 = NotificationSetting.builder()
                .apartmentId(104L)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            settingRepository.save(setting2);
            entityManager.flush();
        });
    }

    @Test
    void testDifferentApartmentsHaveDifferentSettings() {
        NotificationSetting setting1 = NotificationSetting.builder()
                .apartmentId(105L)
                .retentionDays(30)
                .build();
        settingRepository.save(setting1);

        NotificationSetting setting2 = NotificationSetting.builder()
                .apartmentId(106L)
                .retentionDays(180)
                .build();
        settingRepository.save(setting2);

        entityManager.flush();

        NotificationSetting found1 = settingRepository.findById(setting1.getId()).orElseThrow();
        NotificationSetting found2 = settingRepository.findById(setting2.getId()).orElseThrow();

        assertEquals(30, found1.getRetentionDays());
        assertEquals(180, found2.getRetentionDays());
    }
}
