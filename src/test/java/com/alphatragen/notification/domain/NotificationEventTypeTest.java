package com.alphatragen.notification.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testEnumDeserialization() throws Exception {
        // Test valid event type name
        NotificationEventType type = objectMapper.readValue("\"COMPLAINT_STATUS_CHANGED\"", NotificationEventType.class);
        assertEquals(NotificationEventType.COMPLAINT_STATUS_CHANGED, type);

        // Test valid description deserialization via fromValue
        NotificationEventType typeDesc = NotificationEventType.fromValue("민원 답변 등록");
        assertEquals(NotificationEventType.COMPLAINT_ANSWER_REGISTERED, typeDesc);

        // Test invalid value throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationEventType.fromValue("UNKNOWN_EVENT");
        });
    }

    @Test
    void testImportancePolicy() {
        assertEquals(NotificationImportance.NORMAL, NotificationEventType.COMPLAINT_STATUS_CHANGED.getDefaultImportance());
        assertEquals(NotificationImportance.URGENT, NotificationEventType.URGENT_NOTICE.getDefaultImportance());
        assertEquals(NotificationImportance.NORMAL, NotificationEventType.OFFICE_MANUAL_SEND.getDefaultImportance());
    }
}
