package com.example.domain.log.event;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.event.ErrorMeta;
import com.example.global.exception.BaseAppException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.SensitiveLogMessageSanitizer;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 예외 발생 시, 예외 정보를 담는 이벤트 객체
 *
 * <p>
 * [네이밍 규칙 표준화]
 * - from(...): 예외/요청 정보로부터 이벤트 객체로 "변환"하는 경우
 * - 포맷팅(로그 문자열, 구조화 로그)은 {@link ExceptionEventFormatter}에 위임합니다.
 * </p>
 */
public record ExceptionEvent(
        String traceId,
        String requestPath,
        String requestMethod,
        String errorName,
        ErrorCode errorCode,
        String errorDetailMsg,
        String debugStackTrace,
        CurrentAccountDTO account,
        LocalDateTime createdAt,
        String clientIp,
        List<ApiErrorDetail> validationErrors
) {

    public ExceptionEvent {
        errorDetailMsg = sanitizeMessage(errorDetailMsg);
        debugStackTrace = sanitizeMessage(debugStackTrace);
        validationErrors = validationErrors != null ? List.copyOf(validationErrors) : List.of();
    }

    public static ExceptionEvent of(
            final String traceId,
            final String requestPath,
            final String requestMethod,
            final String errorName,
            final ErrorCode errorCode,
            final String errorDetailMsg,
            final String debugStackTrace,
            final CurrentAccountDTO account,
            final LocalDateTime createdAt,
            final String clientIp,
            final List<ApiErrorDetail> validationErrors
    ) {
        return new ExceptionEvent(traceId, requestPath, requestMethod, errorName, errorCode,
                errorDetailMsg, debugStackTrace, account, createdAt, clientIp, validationErrors);
    }

    public static ExceptionEvent from(final ExceptionContext context) {
        return from(context, TraceIdUtils.resolveTraceId());
    }

    public static ExceptionEvent from(final ExceptionContext context, final String traceId) {
        final Exception exception = context.exception();
        final HttpServletRequest request = context.httpServletRequest();

        final String requestPath = request != null ? request.getRequestURL().toString() : "";
        final String requestMethod = request != null ? request.getMethod() : "";
        final String clientIp = request != null ? ClientIpExtractor.extract(request) : "";

        final String errorName = exception != null ? exception.getClass().getSimpleName() : "UnknownException";
        final String resolvedDebugStackTrace = resolveDebugStackTrace(exception);

        return new ExceptionEvent(
                traceId != null ? traceId : TraceIdUtils.resolveTraceId(),
                requestPath,
                requestMethod,
                errorName,
                context.errorCode(),
                context.errorDetailMsg(),
                resolvedDebugStackTrace,
                context.account(),
                LocalDateTime.now(),
                clientIp,
                context.validationErrors()
        );
    }

    public static ExceptionEvent from(final Exception exception, final ErrorCode errorCode, final String errorDetailMsg, final CurrentAccountDTO account, final HttpServletRequest httpServletRequest) {
        return from(ExceptionContext.of(exception, errorCode, errorDetailMsg, account, httpServletRequest));
    }

    public static ExceptionEvent from(final Exception exception, final ErrorCode errorCode, final String errorDetailMsg, final CurrentAccountDTO account, final HttpServletRequest httpServletRequest, final String traceId) {
        return from(ExceptionContext.of(exception, errorCode, errorDetailMsg, account, httpServletRequest), traceId);
    }

    /**
     * 예외 타입에 맞는 ErrorCode/메시지를 자동 해석하는 표준 팩토리
     */
    public static ExceptionEvent from(final Exception exception, final CurrentAccountDTO account, final HttpServletRequest httpServletRequest) {
        final ErrorMeta meta = ExceptionEventMapper.resolveErrorMeta(exception);
        return from(ExceptionContext.of(exception, meta.errorCode(), meta.detailMessage(), account, httpServletRequest));
    }

    static String resolveDetailMessage(final String message, final ErrorCode fallbackErrorCode) {
        if (message != null && !message.isBlank()) {
            return sanitizeMessage(message);
        }
        return sanitizeMessage(fallbackErrorCode != null ? fallbackErrorCode.getErrorMessage() : "");
    }

    private static String resolveDebugStackTrace(final Exception exception) {
        if (exception instanceof BaseAppException appException) {
            return appException.getDebugStackTrace();
        }
        return null;
    }

    private static String sanitizeMessage(final String message) {
        return SensitiveLogMessageSanitizer.sanitize(message);
    }

    /**
     * 예외 이벤트를 사람이 읽기 좋은 문자열 형태로 변환합니다.
     *
     * @see ExceptionEventFormatter#toLogString(ExceptionEvent)
     */
    public String toLogString() {
        return ExceptionEventFormatter.toLogString(this);
    }

    /**
     * 예외 이벤트를 구조화된(JSON) 로그용 Map으로 변환합니다.
     *
     * @see ExceptionEventFormatter#toStructuredLog(ExceptionEvent)
     */
    public Map<String, Object> getStructuredLog() {
        return ExceptionEventFormatter.toStructuredLog(this);
    }
}
