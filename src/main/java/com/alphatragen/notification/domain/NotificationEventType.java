package com.alphatragen.notification.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationEventType {
    COMPLAINT_STATUS_CHANGED("Complaint status changed", NotificationImportance.NORMAL),
    COMPLAINT_ANSWERED("Complaint answered", NotificationImportance.NORMAL),
    FACILITY_REQUEST_APPROVED("Facility request approved", NotificationImportance.NORMAL),
    FACILITY_REQUEST_REJECTED("Facility request rejected", NotificationImportance.NORMAL),
    FACILITY_RESERVATION_CANCELLED_BY_ADMIN("Facility reservation cancelled by admin", NotificationImportance.NORMAL),
    POST_COMMENT_CREATED("Post comment created", NotificationImportance.NORMAL),
    CLUB_JOIN_APPROVED("Club join approved", NotificationImportance.NORMAL),
    CLUB_JOIN_REJECTED("Club join rejected", NotificationImportance.NORMAL),
    CLUB_NOTICE_CREATED("Club notice created", NotificationImportance.NORMAL),
    VOTE_STARTED("Vote started", NotificationImportance.NORMAL),
    VOTE_END_IMMINENT("Vote end imminent", NotificationImportance.NORMAL),
    VOTE_RESULT_PUBLISHED("Vote result published", NotificationImportance.NORMAL),
    MAINTENANCE_FEE_PAYMENT_CONFIRMED("Maintenance fee payment confirmed", NotificationImportance.NORMAL),
    NOTICE_CREATED("Notice created", NotificationImportance.URGENT),
    MISSING_PERSON_DETECTED("Missing person detected", NotificationImportance.URGENT),
    USER_WITHDRAWN("User withdrawn", NotificationImportance.NORMAL),
    OFFICE_MANUAL_SEND("Office manual send", NotificationImportance.NORMAL);

    private final String description;
    private final NotificationImportance defaultImportance;

    NotificationEventType(String description, NotificationImportance defaultImportance) {
        this.description = description;
        this.defaultImportance = defaultImportance;
    }

    public String getDescription() {
        return description;
    }

    public NotificationImportance getDefaultImportance() {
        return defaultImportance;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static NotificationEventType fromValue(String value) {
        for (NotificationEventType type : NotificationEventType.values()) {
            if (type.name().equalsIgnoreCase(value) || type.getDescription().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event type: " + value);
    }
}
