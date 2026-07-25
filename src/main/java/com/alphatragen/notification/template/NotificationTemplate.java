package com.alphatragen.notification.template;

import com.alphatragen.notification.domain.NotificationEventType;

import java.util.EnumMap;
import java.util.List;

public enum NotificationTemplate {
    COMPLAINT_STATUS_CHANGED(
            NotificationEventType.COMPLAINT_STATUS_CHANGED,
            "Complaint status updated",
            "Your complaint status changed to {status}.",
            List.of("status")
    ),
    COMPLAINT_ANSWERED(
            NotificationEventType.COMPLAINT_ANSWERED,
            "Complaint answered",
            "An administrator answered your complaint.",
            List.of()
    ),
    FACILITY_REQUEST_APPROVED(
            NotificationEventType.FACILITY_REQUEST_APPROVED,
            "Facility request approved",
            "{facilityName} request has been approved.",
            List.of("facilityName")
    ),
    FACILITY_REQUEST_REJECTED(
            NotificationEventType.FACILITY_REQUEST_REJECTED,
            "Facility request rejected",
            "{facilityName} request has been rejected. Reason: {reason}",
            List.of("facilityName", "reason")
    ),
    FACILITY_RESERVATION_CANCELLED_BY_ADMIN(
            NotificationEventType.FACILITY_RESERVATION_CANCELLED_BY_ADMIN,
            "Facility reservation cancelled",
            "{facilityName} reservation was cancelled by an administrator. Reason: {reason}",
            List.of("facilityName", "reason")
    ),
    POST_COMMENT_CREATED(
            NotificationEventType.POST_COMMENT_CREATED,
            "New comment on your post",
            "{commentAuthorName}: {commentPreview}",
            List.of("commentAuthorName", "commentPreview")
    ),
    CLUB_JOIN_APPROVED(
            NotificationEventType.CLUB_JOIN_APPROVED,
            "Club join request approved",
            "Your request to join {communityName} has been approved.",
            List.of("communityName")
    ),
    CLUB_JOIN_REJECTED(
            NotificationEventType.CLUB_JOIN_REJECTED,
            "Club join request rejected",
            "Your request to join {communityName} has been rejected. Reason: {reason}",
            List.of("communityName", "reason")
    ),
    CLUB_NOTICE_CREATED(
            NotificationEventType.CLUB_NOTICE_CREATED,
            "New club notice",
            "[{communityName}] {noticeTitle}",
            List.of("communityName", "noticeTitle")
    ),
    VOTE_STARTED(
            NotificationEventType.VOTE_STARTED,
            "New vote started",
            "{voteTitle} has started. Please participate by {endAt}.",
            List.of("voteTitle")
    ),
    VOTE_END_IMMINENT(
            NotificationEventType.VOTE_END_IMMINENT,
            "Vote ending soon",
            "{voteTitle} is ending soon. Please participate if you have not voted.",
            List.of("voteTitle")
    ),
    VOTE_RESULT_PUBLISHED(
            NotificationEventType.VOTE_RESULT_PUBLISHED,
            "Vote result published",
            "{voteTitle} result has been published.",
            List.of("voteTitle")
    ),
    MAINTENANCE_FEE_PAYMENT_CONFIRMED(
            NotificationEventType.MAINTENANCE_FEE_PAYMENT_CONFIRMED,
            "Maintenance fee payment confirmed",
            "{billingMonth} maintenance fee payment of {paidAmount} has been confirmed.",
            List.of("maintenanceFeeId", "householdId", "billingMonth", "paidAmount", "paidAt")
    ),
    MAINTENANCE_FEE_NOTIFIED(
        NotificationEventType.MAINTENANCE_FEE_NOTIFIED,
        "관리비 고지 안내",
        "{billingMonth} 관리비 {totalAmount}이(가) 고지되었습니다. 납부기한: {dueDate}까지",
        List.of("maintenanceFeeId", "billingMonth", "totalAmount", "dueDate")
    ),
    OBJECTION_NO_ERROR(
        NotificationEventType.MAINTENANCE_FEE_OBJECTION_NO_ERROR,
        "이의신청 처리결과: 오류없음",
        "{billingMonth} 관리비 이의신청이 검토되었습니다. 처리결과: {reviewResult}",
        List.of("objectionId", "billingMonth", "reviewResult")
    ),
    OBJECTION_CORRECTED(
        NotificationEventType.MAINTENANCE_FEE_OBJECTION_CORRECTED,
        "이의신청 처리결과: 정정반영",
        "{billingMonth} 관리비 이의신청이 검토되어 {adjustmentAmount}원이 정정 반영됩니다. {reviewResult}",
        List.of("objectionId", "billingMonth", "adjustmentAmount", "reviewResult")
    ),
    NOTICE_CREATED(
            NotificationEventType.NOTICE_CREATED,
            "[Urgent] {noticeTitle}",
            "{noticeContent}",
            List.of("noticeTitle", "noticeContent")
    ),
    OFFICE_MANUAL_SEND(
            NotificationEventType.OFFICE_MANUAL_SEND,
            "{title}",
            "{content}",
            List.of("title", "content")
    );

    private static final EnumMap<NotificationEventType, NotificationTemplate> TEMPLATES_BY_EVENT_TYPE;

    static {
        TEMPLATES_BY_EVENT_TYPE = new EnumMap<>(NotificationEventType.class);
        for (NotificationTemplate template : values()) {
            TEMPLATES_BY_EVENT_TYPE.put(template.eventType, template);
        }
    }

    private final NotificationEventType eventType;
    private final String titleTemplate;
    private final String contentTemplate;
    private final List<String> requiredKeys;

    NotificationTemplate(
            NotificationEventType eventType,
            String titleTemplate,
            String contentTemplate,
            List<String> requiredKeys
    ) {
        this.eventType = eventType;
        this.titleTemplate = titleTemplate;
        this.contentTemplate = contentTemplate;
        this.requiredKeys = requiredKeys;
    }

    public static NotificationTemplate from(NotificationEventType eventType) {
        NotificationTemplate template = TEMPLATES_BY_EVENT_TYPE.get(eventType);
        if (template == null) {
            throw new IllegalArgumentException("Unsupported event type for template rendering: " + eventType);
        }
        return template;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public List<String> getRequiredKeys() {
        return requiredKeys;
    }
}
