package com.example.global.utils;

import com.example.global.payload.response.ApiErrorDetail;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 로그인 관련 로그 처리 유틸
 */
public final class LoginLoggingUtils {

    public static final String DEFAULT_UNKNOWN_LOGIN_ID = "UNKNOWN";

    private LoginLoggingUtils() {
    }

    public static Optional<String> extractLoginId(final HttpServletRequest request, final String paramName, final String attributeName) {
        if (request == null) {
            return Optional.empty();
        }

        final String loginId = request.getParameter(paramName);
        if (StringCheckUtils.hasText(loginId)) {
            return Optional.of(loginId);
        }

        final Object attribute = request.getAttribute(attributeName);
        if (attribute instanceof String attrLoginId && StringCheckUtils.hasText(attrLoginId)) {
            return Optional.of(attrLoginId);
        }

        return Optional.empty();
    }

    public static String resolveLoginIdOrDefault(
            final HttpServletRequest request,
            final String paramName,
            final String attributeName,
            final String defaultValue
    ) {
        return extractLoginId(request, paramName, attributeName).orElse(defaultValue);
    }

    /**
     * 에러 상세 목록을 로그용 문자열로 변환합니다.
     * <p>
     * 최대 5개까지만 표시하며, 초과분은 "{@code , ...}"로 생략합니다.
     * null 또는 빈 리스트가 전달되면 "{@code []}"을 반환합니다.
     * 리스트 내부의 null 요소는 건너뜁니다.
     *
     * @param errors 에러 상세 목록 (null 허용)
     * @return {@code [field1=reason1, field2=reason2]} 형식의 문자열
     */
    public static String formatErrors(final List<ApiErrorDetail> errors) {
        if (errors == null || errors.isEmpty()) {
            return "[]";
        }

        final int limit = 5;
        final String joined = errors.stream()
                .limit(limit)
                .filter(Objects::nonNull)
                .map(e -> safe(e.field()) + "=" + safe(e.reason()))
                .collect(Collectors.joining(", "));

        final String suffix = errors.size() > limit ? ", ..." : "";
        return "[" + joined + suffix + "]";
    }

    /**
     * null 안전 문자열 래퍼
     * <p>
     * null이 전달되면 빈 문자열({@code ""})을 반환하고, 그 외에는 원본을 그대로 반환합니다.
     *
     * @param value 변환할 문자열 (null 허용)
     * @return null이면 빈 문자열, 아니면 원본 문자열
     */
    public static String safe(final String value) {
        return value == null ? "" : value;
    }

}
