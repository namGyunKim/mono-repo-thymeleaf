package com.example.domain.log.payload.request;

import com.example.domain.contract.enums.ApiLogType;
import com.example.domain.log.enums.LogType;
import com.example.global.utils.PaginationUtils;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 회원 활동 로그 조회 요청 DTO
 *
 * <p>
 * 베이스 프로젝트에서는 화면/쿼리스트링 조작 등으로 page/size가 비정상 값으로 들어와도
 * 서비스에서 예외가 발생하지 않도록 기본값/보정을 수행합니다.
 * </p>
 */
public record MemberLogRequest(
        @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
        Integer page,

        @Min(value = 1, message = "페이지 사이즈는 1 이상이어야 합니다.")
        Integer size,

        String loginId,

        Long memberId,

        ApiLogType logType,

        String details,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAt,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endAt
) {

    public MemberLogRequest {
        page = PaginationUtils.normalizePage(page);
        size = PaginationUtils.normalizeSize(size, PaginationUtils.DEFAULT_LOG_SIZE);
        loginId = normalizeKeyword(loginId);
        details = normalizeKeyword(details);
    }

    public static MemberLogRequest of(final Integer page, final Integer size, final String loginId, final Long memberId, final ApiLogType logType, final String details, final LocalDateTime startAt, final LocalDateTime endAt) {
        return new MemberLogRequest(page, size, loginId, memberId, logType, details, startAt, endAt);
    }

    public LogType toDomainLogType() {
        return logType != null ? logType.toDomain() : null;
    }

    private static String normalizeKeyword(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
