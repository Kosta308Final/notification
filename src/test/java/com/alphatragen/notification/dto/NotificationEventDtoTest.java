package com.alphatragen.notification.dto;

import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.domain.NotificationTargetType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidDto() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-123",
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);
        dto.setActionUrl("/complaints/25");

        Set<ConstraintViolation<NotificationEventDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertDoesNotThrow(dto::validateTargetSpecificFields);
    }

    @Test
    void testMissingRequiredFields() {
        NotificationEventDto dto = new NotificationEventDto();
        Set<ConstraintViolation<NotificationEventDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testIndividualTargetValidationWithoutUserId() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-123",
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );

        assertThrows(IllegalArgumentException.class, dto::validateTargetSpecificFields);
    }

    @Test
    void testHouseholdTargetValidationWithoutUnit() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-123",
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.HOUSEHOLD
        );
        dto.setBuilding("101");
        // Unit is missing!

        assertThrows(IllegalArgumentException.class, dto::validateTargetSpecificFields);
    }

    @Test
    void testRoleTargetValidationWithoutRole() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-123",
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.ROLE
        );

        assertThrows(IllegalArgumentException.class, dto::validateTargetSpecificFields);
    }

    @Test
    void testInvalidActionUrl() {
        NotificationEventDto dto = new NotificationEventDto(
                "evt-123",
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                LocalDateTime.now(),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        dto.setUserId(100L);
        dto.setActionUrl("http://external-site.com/complaints"); // Invalid, external

        Set<ConstraintViolation<NotificationEventDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
