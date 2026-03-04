package com.example.domain.log.support;

import com.example.global.payload.response.ApiErrorDetail;

import java.util.List;
import java.util.StringJoiner;

/**
 * {@link ApiErrorDetail} 리스트를 JSON 문자열로 직렬화하는 유틸리티
 *
 * <p>
 * 도메인 레이어의 Jackson 의존을 최소화하기 위해 단순 문자열 조합으로 처리한다.
 * {@code ApiErrorDetail}은 필드 2개(field, reason)뿐이므로 충분하다.
 * </p>
 */
public final class ValidationErrorSerializer {

    private ValidationErrorSerializer() {
    }

    /**
     * 검증 에러 리스트를 JSON 배열 문자열로 변환한다.
     *
     * @param errors 검증 에러 리스트 (null 또는 빈 리스트이면 null 반환)
     * @return JSON 문자열 또는 null
     */
    public static String serialize(final List<ApiErrorDetail> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        final StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (final ApiErrorDetail error : errors) {
            joiner.add("{\"field\":\"%s\",\"reason\":\"%s\"}".formatted(
                    escapeJson(error.field()), escapeJson(error.reason())));
        }
        return joiner.toString();
    }

    private static String escapeJson(final String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
