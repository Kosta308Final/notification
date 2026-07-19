package com.alphatragen.notification.template;

import com.alphatragen.notification.domain.NotificationEventType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    @Override
    public TemplateResult generate(NotificationEventType eventType, Map<String, Object> templateData) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }

        switch (eventType) {
            case COMPLAINT_STATUS_CHANGED: {
                validateRequired(templateData, "status");
                String status = String.valueOf(templateData.get("status"));
                return new TemplateResult(
                        "민원 처리 상태가 변경되었습니다.",
                        "등록하신 민원의 상태가 [" + status + "](으)로 변경되었습니다."
                );
            }
            case COMPLAINT_ANSWER_REGISTERED: {
                return new TemplateResult(
                        "민원에 답변이 등록되었습니다.",
                        "등록하신 민원에 대한 답변이 등록되었습니다."
                );
            }
            case FACILITY_REQUEST_APPROVED: {
                validateRequired(templateData, "facilityName");
                String facilityName = String.valueOf(templateData.get("facilityName"));
                return new TemplateResult(
                        "시설 신청이 승인되었습니다.",
                        facilityName + " 신청이 승인되었습니다."
                );
            }
            case FACILITY_REQUEST_REJECTED: {
                validateRequired(templateData, "facilityName", "reason");
                String facilityName = String.valueOf(templateData.get("facilityName"));
                String reason = String.valueOf(templateData.get("reason"));
                return new TemplateResult(
                        "시설 신청이 거절되었습니다.",
                        facilityName + " 신청이 거절되었습니다. 사유: " + reason
                );
            }
            case FACILITY_RESERVATION_CANCELLED_BY_ADMIN: {
                validateRequired(templateData, "facilityName", "reason");
                String facilityName = String.valueOf(templateData.get("facilityName"));
                String reason = String.valueOf(templateData.get("reason"));
                return new TemplateResult(
                        "시설 예약이 취소되었습니다.",
                        facilityName + " 예약이 관리자에 의해 취소되었습니다. 사유: " + reason
                );
            }
            case VOTE_STARTED: {
                validateRequired(templateData, "voteTitle");
                String voteTitle = String.valueOf(templateData.get("voteTitle"));
                return new TemplateResult(
                        "새로운 투표가 시작되었습니다.",
                        "[" + voteTitle + "] 투표가 시작되었습니다. 참여해 주세요."
                );
            }
            case VOTE_END_IMMINENT: {
                validateRequired(templateData, "voteTitle");
                String voteTitle = String.valueOf(templateData.get("voteTitle"));
                return new TemplateResult(
                        "투표 참여 안내",
                        "[" + voteTitle + "] 투표 종료가 임박했습니다. 아직 참여하지 않으셨다면 참여해 주세요."
                );
            }
            case VOTE_RESULT_PUBLISHED: {
                validateRequired(templateData, "voteTitle");
                String voteTitle = String.valueOf(templateData.get("voteTitle"));
                return new TemplateResult(
                        "투표 결과가 공개되었습니다.",
                        "[" + voteTitle + "] 투표 결과가 공개되었습니다. 확인해 주세요."
                );
            }
            case URGENT_NOTICE: {
                validateRequired(templateData, "noticeTitle", "noticeContent");
                String noticeTitle = String.valueOf(templateData.get("noticeTitle"));
                String noticeContent = String.valueOf(templateData.get("noticeContent"));
                return new TemplateResult(
                        "[긴급] " + noticeTitle,
                        noticeContent
                );
            }
            case OFFICE_MANUAL_SEND: {
                validateRequired(templateData, "title", "content");
                String title = String.valueOf(templateData.get("title"));
                String content = String.valueOf(templateData.get("content"));
                return new TemplateResult(title, content);
            }
            default:
                throw new IllegalArgumentException("Unsupported event type for template rendering: " + eventType);
        }
    }

    private void validateRequired(Map<String, Object> templateData, String... requiredKeys) {
        if (templateData == null) {
            throw new IllegalArgumentException("Template data cannot be null");
        }
        for (String key : requiredKeys) {
            if (!templateData.containsKey(key) || templateData.get(key) == null || String.valueOf(templateData.get(key)).trim().isEmpty()) {
                throw new IllegalArgumentException("Missing required template variable: " + key);
            }
        }
    }
}
