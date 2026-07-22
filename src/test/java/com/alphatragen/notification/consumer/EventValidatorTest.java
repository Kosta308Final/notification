package com.alphatragen.notification.consumer;

import com.alphatragen.notification.domain.NotificationEventType;
import com.alphatragen.notification.domain.NotificationTargetType;
import com.alphatragen.notification.dto.NotificationEventDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EventValidatorTest {

    private final EventValidator validator = new EventValidator();

    @Test
    void acceptsCommentFragmentActionUrlPublishedByApartmentServer() {
        NotificationEventDto event = new NotificationEventDto(
                "evt-1",
                NotificationEventType.POST_COMMENT_CREATED,
                LocalDateTime.of(2026, 7, 21, 22, 0),
                1L,
                NotificationTargetType.INDIVIDUAL
        );
        event.setUserId(7L);
        event.setActionUrl("/board/posts/3#comment-4");

        assertDoesNotThrow(() -> validator.validate(event));
    }
}
