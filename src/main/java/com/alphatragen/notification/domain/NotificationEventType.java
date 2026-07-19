package com.alphatragen.notification.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationEventType {
    COMPLAINT_STATUS_CHANGED("민원 상태 변경", NotificationImportance.NORMAL),
    COMPLAINT_ANSWER_REGISTERED("민원 답변 등록", NotificationImportance.NORMAL),
    FACILITY_REQUEST_APPROVED("시설 신청 승인", NotificationImportance.NORMAL),
    FACILITY_REQUEST_REJECTED("시설 신청 거절", NotificationImportance.NORMAL),
    FACILITY_RESERVATION_CANCELLED_BY_ADMIN("시설 예약 관리자 취소", NotificationImportance.NORMAL),
    VOTE_STARTED("투표 시작", NotificationImportance.NORMAL),
    VOTE_END_IMMINENT("투표 종료 임박", NotificationImportance.NORMAL),
    VOTE_RESULT_PUBLISHED("투표 결과 공개", NotificationImportance.NORMAL),
    URGENT_NOTICE("긴급 공지", NotificationImportance.URGENT),
    USER_WITHDRAWAL("회원 탈퇴", NotificationImportance.NORMAL),
    OFFICE_MANUAL_SEND("관리사무소 수동 발송", NotificationImportance.NORMAL);

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
