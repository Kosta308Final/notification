package com.alphatragen.notification.repository;

import com.alphatragen.notification.domain.NotificationSetting;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationSettingRepositoryTest {

    @Autowired
    private NotificationSettingRepository settingRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveSettingSuccess() {
        NotificationSetting setting = new NotificationSetting();
        setting.setApartmentId(101L);
        setting.setRetentionDays(120);

        NotificationSetting saved = settingRepository.save(setting);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals(101L, saved.getApartmentId());
        assertEquals(120, saved.getRetentionDays());
    }

    @Test
    void testDefaultRetentionDaysIs90() {
        NotificationSetting setting = new NotificationSetting();
        setting.setApartmentId(102L);

        NotificationSetting saved = settingRepository.save(setting);
        entityManager.flush();

        assertEquals(90, saved.getRetentionDays());
    }

    @Test
    void testRetentionDaysOutOfRangeThrowsException() {
        NotificationSetting setting = new NotificationSetting();
        setting.setApartmentId(103L);

        // Less than 30
        assertThrows(IllegalArgumentException.class, () -> setting.setRetentionDays(29));

        // Greater than 365
        assertThrows(IllegalArgumentException.class, () -> setting.setRetentionDays(366));
    }

    @Test
    void testDuplicateApartmentIdThrowsException() {
        NotificationSetting setting1 = new NotificationSetting();
        setting1.setApartmentId(104L);
        settingRepository.save(setting1);
        entityManager.flush();

        NotificationSetting setting2 = new NotificationSetting();
        setting2.setApartmentId(104L); // Duplicate apartmentId

        assertThrows(DataIntegrityViolationException.class, () -> {
            settingRepository.save(setting2);
            entityManager.flush();
        });
    }

    @Test
    void testDifferentApartmentsHaveDifferentSettings() {
        NotificationSetting setting1 = new NotificationSetting();
        setting1.setApartmentId(105L);
        setting1.setRetentionDays(30);
        settingRepository.save(setting1);

        NotificationSetting setting2 = new NotificationSetting();
        setting2.setApartmentId(106L);
        setting2.setRetentionDays(180);
        settingRepository.save(setting2);

        entityManager.flush();

        NotificationSetting found1 = settingRepository.findById(setting1.getId()).orElseThrow();
        NotificationSetting found2 = settingRepository.findById(setting2.getId()).orElseThrow();

        assertEquals(30, found1.getRetentionDays());
        assertEquals(180, found2.getRetentionDays());
    }
}
