package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 회원 활동 로그 검색용 조회 DTO
 */
public record MemberLogSearchQuery(
        String loginId,
        Long memberId,
        LogType logType,
        String details,
        LocalDateTime startAt,
        LocalDateTime endAt
) {

    public MemberLogSearchQuery {
        loginId = normalizeKeyword(loginId);
        details = normalizeKeyword(details);
        startAt = Objects.requireNonNullElse(startAt, LocalDateTime.of(1970, 1, 1, 0, 0));
        endAt = Objects.requireNonNullElse(endAt, LocalDateTime.of(9999, 12, 31, 23, 59, 59));
    }

    public static MemberLogSearchQuery of(final String loginId, final Long memberId, final LogType logType, final String details, final LocalDateTime startAt, final LocalDateTime endAt) {
        return new MemberLogSearchQuery(loginId, memberId, logType, details, startAt, endAt);
    }

    public static MemberLogSearchQuery from(final MemberLogQuery query) {
        if (query == null) {
            return of("", null, null, "", null, null);
        }
        return of(query.loginId(), query.memberId(), query.logType(), query.details(), query.startAt(), query.endAt());
    }

    private static String normalizeKeyword(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
