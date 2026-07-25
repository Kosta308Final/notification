package com.alphatragen.notification.consumer;

import com.alphatragen.notification.dto.NotificationEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventDeserializerTest {

    private final EventDeserializer deserializer = new EventDeserializer(new ObjectMapper());

    @Test
    void parsesScientificNotationEpochSeconds() throws Exception {
        NotificationEventDto result = deserializer.deserialize("""
                {
                  "eventId": "evt-1",
                  "eventType": "NOTICE_CREATED",
                  "occurredAt": 1.784635668028431E9,
                  "apartmentId": 1,
                  "targetType": "APARTMENT",
                  "templateData": {}
                }
                """);

        assertThat(result.getOccurredAt()).isEqualTo(LocalDateTime.of(2026, 7, 21, 21, 7, 48, 28_431_000));
    }

    @Test
    void parsesEpochMillis() throws Exception {
        NotificationEventDto result = deserializer.deserialize("""
                {
                  "eventId": "evt-2",
                  "eventType": "NOTICE_CREATED",
                  "occurredAt": 1784635668028,
                  "apartmentId": 1,
                  "targetType": "APARTMENT",
                  "templateData": {}
                }
                """);

        assertThat(result.getOccurredAt()).isEqualTo(LocalDateTime.of(2026, 7, 21, 21, 7, 48, 28_000_000));
    }

    @Test
    void parsesBackendNotificationEventShapeWithNestedRecipient() throws Exception {
        NotificationEventDto result = deserializer.deserialize("""
                {
                  "eventId": "4d2f5bb1-c9d5-4d6a-b4a4-4b2b7df7ce12",
                  "eventType": "MISSING_PERSON_DETECTED",
                  "occurredAt": "2026-07-25T14:31:12+09:00",
                  "sourceService": "apartment-service",
                  "apartmentId": 1,
                  "recipient": {
                    "type": "INDIVIDUAL",
                    "userId": 2001,
                    "building": null,
                    "unit": null,
                    "role": null
                  },
                  "templateData": {
                    "missingPersonId": 101,
                    "detectionRequestId": "det-20260725-0001",
                    "detailId": 1,
                    "cameraName": "정문 앞 CCTV",
                    "cameraAddress": "서울시 강남구 예시로 101, 아파트 정문",
                    "imageUrl": "/mock/missing-person/gate-front-001.jpg"
                  },
                  "actionUrl": "/missing-person/detections/1",
                  "urgent": true
                }
                """);

        assertThat(result.getEventType().name()).isEqualTo("MISSING_PERSON_DETECTED");
        assertThat(result.getTargetType().name()).isEqualTo("INDIVIDUAL");
        assertThat(result.getUserId()).isEqualTo(2001L);
        assertThat(result.getTemplateData())
                .containsEntry("cameraName", "정문 앞 CCTV")
                .containsEntry("detailId", "1");
        assertThat(result.getActionUrl()).isEqualTo("/missing-person/detections/1");
    }
}
