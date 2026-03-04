package com.example.domain.log.event;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * {@link ExceptionEvent}의 포맷팅(로그 문자열, 구조화 로그) 책임을 담당합니다.
 * - ExceptionEvent는 순수 데이터 보관(record)에 집중하고,
 * 표현(포맷)은 이 클래스의 static 메서드로 분리합니다.
 */
public final class ExceptionEventFormatter {

    private ExceptionEventFormatter() {
    }

    /**
     * 예외 이벤트를 사람이 읽기 좋은 문자열 형태로 변환합니다.
     */
    public static String toLogString(final ExceptionEvent event) {
        return """
                logStart=== === === === === === === === === === === === === === === === === === === === === === === === logStart
                Trace ID : %s
                Exception Title : %s
                Request Path : %s
                Request Method : %s
                Client IP : %s
                %s%screateDate : %s

                %s
                %s%s
                logEnd=== === === === === === === === === === === === === === === === === === === === === === === === logEnd
                """.formatted(
                nullToEmpty(event.traceId()),
                nullToEmpty(event.errorName()),
                nullToEmpty(event.requestPath()),
                nullToEmpty(event.requestMethod()),
                nullToEmpty(event.clientIp()),
                buildAccountBlock(event.account()),
                buildErrorCodeBlock(event.errorCode()),
                event.createdAt() != null ? event.createdAt().toString() : "",
                nullToEmpty(event.errorDetailMsg()),
                buildDebugBlock(event.debugStackTrace()),
                buildValidationErrorsBlock(event.validationErrors())
        ).stripTrailing();
    }

    /**
     * 예외 이벤트를 구조화된(JSON) 로그용 Map으로 변환합니다.
     */
    public static Map<String, Object> toStructuredLog(final ExceptionEvent event) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", "exception");
        payload.put("traceId", event.traceId());
        payload.put("errorName", event.errorName());
        payload.put("errorCode", event.errorCode() != null ? event.errorCode().getCode() : null);
        payload.put("errorMessage", event.errorCode() != null ? event.errorCode().getErrorMessage() : null);
        payload.put("errorDetailMessage", event.errorDetailMsg());
        payload.put("debugStackTrace", event.debugStackTrace());
        payload.put("requestPath", event.requestPath());
        payload.put("requestMethod", event.requestMethod());
        payload.put("clientIp", event.clientIp());
        payload.put("createdAt", event.createdAt() != null ? event.createdAt().toString() : null);
        payload.put("account", resolveAccountPayload(event.account()));
        payload.put("validationErrors", resolveValidationErrorsPayload(event.validationErrors()));
        return payload;
    }

    private static String buildAccountBlock(final CurrentAccountDTO account) {
        if (account == null) {
            return "";
        }
        return """
                Account Member ID : %s
                Account role : %s
                Account ID : %s
                Account Nickname : %s
                """.formatted(
                account.id() != null ? account.id().toString() : "",
                nullToEmpty(account.role() != null ? account.role().name() : ""),
                nullToEmpty(account.loginId()),
                nullToEmpty(account.nickName())
        );
    }

    private static String buildErrorCodeBlock(final ErrorCode errorCode) {
        if (errorCode == null) {
            return "";
        }
        return """
                Error Code & Msg : %s / %s
                """.formatted(errorCode.getCode(), errorCode.getErrorMessage());
    }

    private static String buildDebugBlock(final String debugStackTrace) {
        if (debugStackTrace == null || debugStackTrace.isBlank()) {
            return "";
        }
        return """
                Debug StackTrace :
                %s
                """.formatted(debugStackTrace);
    }

    private static Map<String, Object> resolveAccountPayload(final CurrentAccountDTO account) {
        if (account == null) {
            return null;
        }
        final Map<String, Object> accountPayload = new LinkedHashMap<>();
        accountPayload.put("id", account.id());
        accountPayload.put("role", account.role() != null ? account.role().name() : null);
        accountPayload.put("loginId", account.loginId());
        accountPayload.put("nickName", account.nickName());
        return accountPayload;
    }

    private static String buildValidationErrorsBlock(final List<ApiErrorDetail> validationErrors) {
        if (validationErrors == null || validationErrors.isEmpty()) {
            return "";
        }
        final StringJoiner sj = new StringJoiner("\n", "\nValidation Errors :\n", "\n");
        for (final ApiErrorDetail error : validationErrors) {
            sj.add("  - field=%s, reason=%s".formatted(
                    nullToEmpty(error.field()),
                    nullToEmpty(error.reason())
            ));
        }
        return sj.toString();
    }

    private static List<Map<String, String>> resolveValidationErrorsPayload(final List<ApiErrorDetail> validationErrors) {
        if (validationErrors == null || validationErrors.isEmpty()) {
            return null;
        }
        return validationErrors.stream()
                .map(error -> {
                    final Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("field", error.field());
                    entry.put("reason", error.reason());
                    return entry;
                })
                .toList();
    }

    private static String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
