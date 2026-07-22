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
}
