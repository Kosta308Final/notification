package com.alphatragen.notification.template;

import com.alphatragen.notification.domain.NotificationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationTemplateServiceTest {

    private NotificationTemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new NotificationTemplateServiceImpl();
    }

    @Test
    void generateWithoutVariables() {
        TemplateResult result = templateService.generate(
                NotificationEventType.COMPLAINT_ANSWERED,
                Map.of()
        );

        assertEquals("Complaint answered", result.getTitle());
        assertEquals("An administrator answered your complaint.", result.getContent());
    }

    @Test
    void generateWithOneVariable() {
        TemplateResult result = templateService.generate(
                NotificationEventType.COMPLAINT_STATUS_CHANGED,
                Map.of("status", "DONE")
        );

        assertEquals("Complaint status updated", result.getTitle());
        assertEquals("Your complaint status changed to DONE.", result.getContent());
    }

    @Test
    void generateWithMultipleVariables() {
        TemplateResult result = templateService.generate(
                NotificationEventType.FACILITY_REQUEST_REJECTED,
                Map.of("facilityName", "Fitness Center", "reason", "Capacity exceeded")
        );

        assertEquals("Facility request rejected", result.getTitle());
        assertEquals("Fitness Center request has been rejected. Reason: Capacity exceeded", result.getContent());
    }

    @Test
    void generateReplacesTitlePlaceholder() {
        TemplateResult result = templateService.generate(
                NotificationEventType.NOTICE_CREATED,
                Map.of("noticeTitle", "Water outage", "noticeContent", "Water is unavailable from 2 PM to 4 PM.")
        );

        assertEquals("[Urgent] Water outage", result.getTitle());
        assertEquals("Water is unavailable from 2 PM to 4 PM.", result.getContent());
    }

    @Test
    void generateMaintenanceFeePaymentConfirmed() {
        TemplateResult result = templateService.generate(
                NotificationEventType.MAINTENANCE_FEE_PAYMENT_CONFIRMED,
                Map.of(
                        "maintenanceFeeId", "900",
                        "householdId", "77",
                        "billingMonth", "2026-07",
                        "paidAmount", "185000",
                        "paidAt", "2026-07-21T10:15:00"
                )
        );

        assertEquals("Maintenance fee payment confirmed", result.getTitle());
        assertEquals("2026-07 maintenance fee payment of 185000 has been confirmed.", result.getContent());
    }

    @Test
    void generateMissingPersonDetectedTemplate() {
        TemplateResult result = templateService.generate(
                NotificationEventType.MISSING_PERSON_DETECTED,
                Map.of(
                        "missingPersonId", "101",
                        "detectionRequestId", "det-20260725-0001",
                        "detailId", "1",
                        "cameraName", "정문 앞 CCTV",
                        "cameraAddress", "서울시 강남구 예시로 101, 아파트 정문"
                )
        );

        assertEquals("실종자 유사 인물 감지", result.getTitle());
        assertEquals("정문 앞 CCTV에서 유사 인물이 감지되었습니다. 위치: 서울시 강남구 예시로 101, 아파트 정문", result.getContent());
    }

    @Test
    void generateThrowsExceptionWhenEventTypeIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.generate(null, Map.of())
        );

        assertEquals("Event type cannot be null", exception.getMessage());
    }

    @Test
    void generateThrowsExceptionWhenEventTypeIsUnsupported() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.generate(NotificationEventType.USER_WITHDRAWN, Map.of())
        );

        assertEquals(
                "Unsupported event type for template rendering: USER_WITHDRAWN",
                exception.getMessage()
        );
    }

    @Test
    void generateThrowsExceptionWhenTemplateDataIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.generate(NotificationEventType.COMPLAINT_ANSWERED, null)
        );

        assertEquals("Template data cannot be null", exception.getMessage());
    }

    @Test
    void generateThrowsExceptionWhenRequiredKeyIsMissing() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.generate(NotificationEventType.FACILITY_REQUEST_APPROVED, Map.of())
        );

        assertEquals("Missing required template variable: facilityName", exception.getMessage());
    }

    @Test
    void generateThrowsExceptionWhenRequiredValueIsNull() {
        Map<String, Object> data = new HashMap<>();
        data.put("facilityName", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.generate(NotificationEventType.FACILITY_REQUEST_APPROVED, data)
        );

        assertEquals("Missing required template variable: facilityName", exception.getMessage());
    }

    @Test
    void generateThrowsExceptionWhenRequiredValueIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.generate(
                        NotificationEventType.FACILITY_REQUEST_APPROVED,
                        Map.of("facilityName", "   ")
                )
        );

        assertEquals("Missing required template variable: facilityName", exception.getMessage());
    }

    @Test
    void generateOfficeManualSendWithTitleAndContentPlaceholders() {
        TemplateResult result = templateService.generate(
                NotificationEventType.OFFICE_MANUAL_SEND,
                Map.of("title", "Manual title", "content", "Manual content")
        );

        assertEquals("Manual title", result.getTitle());
        assertEquals("Manual content", result.getContent());
    }
}
