package com.alphatragen.notification.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationEventTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testEnumDeserialization() throws Exception {
        NotificationEventType type = objectMapper.readValue("\"COMPLAINT_STATUS_CHANGED\"", NotificationEventType.class);
        assertEquals(NotificationEventType.COMPLAINT_STATUS_CHANGED, type);

        NotificationEventType typeDesc = NotificationEventType.fromValue("Complaint answered");
        assertEquals(NotificationEventType.COMPLAINT_ANSWERED, typeDesc);

        assertThrows(IllegalArgumentException.class, () -> NotificationEventType.fromValue("UNKNOWN_EVENT"));
    }

    @Test
    void testImportancePolicy() {
        assertEquals(NotificationImportance.NORMAL, NotificationEventType.COMPLAINT_STATUS_CHANGED.getDefaultImportance());
        assertEquals(NotificationImportance.URGENT, NotificationEventType.NOTICE_CREATED.getDefaultImportance());
        assertEquals(NotificationImportance.URGENT, NotificationEventType.MISSING_PERSON_DETECTED.getDefaultImportance());
        assertEquals(NotificationImportance.NORMAL, NotificationEventType.MAINTENANCE_FEE_PAYMENT_CONFIRMED.getDefaultImportance());
        assertEquals(NotificationImportance.NORMAL, NotificationEventType.OFFICE_MANUAL_SEND.getDefaultImportance());
    }
}
