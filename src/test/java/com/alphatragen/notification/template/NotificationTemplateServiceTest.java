package com.alphatragen.notification.template;

import com.alphatragen.notification.domain.NotificationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTemplateServiceTest {

    private NotificationTemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new NotificationTemplateServiceImpl();
    }

    @Test
    void testComplaintStatusChangedTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "처리완료");

        TemplateResult result = templateService.generate(NotificationEventType.COMPLAINT_STATUS_CHANGED, data);

        assertEquals("민원 처리 상태가 변경되었습니다.", result.getTitle());
        assertEquals("등록하신 민원의 상태가 [처리완료](으)로 변경되었습니다.", result.getContent());
    }

    @Test
    void testComplaintAnswerRegisteredTemplate() {
        Map<String, Object> data = new HashMap<>();

        TemplateResult result = templateService.generate(NotificationEventType.COMPLAINT_ANSWER_REGISTERED, data);

        assertEquals("민원에 답변이 등록되었습니다.", result.getTitle());
        assertEquals("등록하신 민원에 대한 답변이 등록되었습니다.", result.getContent());
    }

    @Test
    void testFacilityRequestApprovedTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("facilityName", "피트니스 센터");

        TemplateResult result = templateService.generate(NotificationEventType.FACILITY_REQUEST_APPROVED, data);

        assertEquals("시설 신청이 승인되었습니다.", result.getTitle());
        assertEquals("피트니스 센터 신청이 승인되었습니다.", result.getContent());
    }

    @Test
    void testFacilityRequestRejectedTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("facilityName", "독서실");
        data.put("reason", "인원 초과");

        TemplateResult result = templateService.generate(NotificationEventType.FACILITY_REQUEST_REJECTED, data);

        assertEquals("시설 신청이 거절되었습니다.", result.getTitle());
        assertEquals("독서실 신청이 거절되었습니다. 사유: 인원 초과", result.getContent());
    }

    @Test
    void testFacilityReservationCancelledTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("facilityName", "게스트하우스");
        data.put("reason", "시설 보수");

        TemplateResult result = templateService.generate(NotificationEventType.FACILITY_RESERVATION_CANCELLED_BY_ADMIN, data);

        assertEquals("시설 예약이 취소되었습니다.", result.getTitle());
        assertEquals("게스트하우스 예약이 관리자에 의해 취소되었습니다. 사유: 시설 보수", result.getContent());
    }

    @Test
    void testVoteStartedTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("voteTitle", "동대표 선거");

        TemplateResult result = templateService.generate(NotificationEventType.VOTE_STARTED, data);

        assertEquals("새로운 투표가 시작되었습니다.", result.getTitle());
        assertEquals("[동대표 선거] 투표가 시작되었습니다. 참여해 주세요.", result.getContent());
    }

    @Test
    void testVoteEndImminentTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("voteTitle", "동대표 선거");

        TemplateResult result = templateService.generate(NotificationEventType.VOTE_END_IMMINENT, data);

        assertEquals("투표 참여 안내", result.getTitle());
        assertEquals("[동대표 선거] 투표 종료가 임박했습니다. 아직 참여하지 않으셨다면 참여해 주세요.", result.getContent());
    }

    @Test
    void testVoteResultPublishedTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("voteTitle", "동대표 선거");

        TemplateResult result = templateService.generate(NotificationEventType.VOTE_RESULT_PUBLISHED, data);

        assertEquals("투표 결과가 공개되었습니다.", result.getTitle());
        assertEquals("[동대표 선거] 투표 결과가 공개되었습니다. 확인해 주세요.", result.getContent());
    }

    @Test
    void testUrgentNoticeTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("noticeTitle", "단수 안내");
        data.put("noticeContent", "오후 2시부터 4시까지 단수됩니다.");

        TemplateResult result = templateService.generate(NotificationEventType.URGENT_NOTICE, data);

        assertEquals("[긴급] 단수 안내", result.getTitle());
        assertEquals("오후 2시부터 4시까지 단수됩니다.", result.getContent());
    }

    @Test
    void testOfficeManualSendTemplate() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "수동 제목");
        data.put("content", "수동 내용");

        TemplateResult result = templateService.generate(NotificationEventType.OFFICE_MANUAL_SEND, data);

        assertEquals("수동 제목", result.getTitle());
        assertEquals("수동 내용", result.getContent());
    }

    @Test
    void testMissingRequiredVariableThrowsException() {
        Map<String, Object> data = new HashMap<>(); // Empty

        assertThrows(IllegalArgumentException.class, () -> {
            templateService.generate(NotificationEventType.FACILITY_REQUEST_APPROVED, data);
        });
    }

    @Test
    void testUnsupportedEventThrowsException() {
        Map<String, Object> data = new HashMap<>();

        // USER_WITHDRAWAL is not supported for rendering
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.generate(NotificationEventType.USER_WITHDRAWAL, data);
        });
    }
}
