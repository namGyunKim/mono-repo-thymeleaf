package com.example.domain.log.payload.response;

import com.example.domain.contract.enums.ApiLogType;
import com.example.domain.log.payload.dto.MemberLogView;
import com.example.global.utils.DateTimeFormatUtils;

/**
 * 로그 이벤트 상세 정보 (유형, 내용, IP, 일시)
 */
public record LogEventResponse(
        ApiLogType logType,
        String details,
        String clientIp,
        String createdAt
) {

    public static LogEventResponse from(final MemberLogView view) {
        if (view == null) {
            throw new IllegalArgumentException("view는 필수입니다.");
        }
        return new LogEventResponse(
                ApiLogType.fromDomain(view.logType()),
                view.details(),
                view.clientIp(),
                DateTimeFormatUtils.formatKoreanDateTime(view.createdAt())
        );
    }

    public static LogEventResponse of(final ApiLogType logType, final String details, final String clientIp, final String createdAt) {
        return new LogEventResponse(logType, details, clientIp, createdAt);
    }
}
