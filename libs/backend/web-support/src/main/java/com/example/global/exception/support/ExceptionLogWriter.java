package com.example.global.exception.support;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.logging.RequestMeta;
import com.example.global.utils.LoggingSanitizerPolicy;
import com.example.global.utils.SensitiveLogMessageSanitizer;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@Component
public class ExceptionLogWriter {

    public RequestMeta resolveRequestMeta(final HttpServletRequest request) {
        return RequestMeta.from(request);
    }

    public void logTypeMismatch(final RequestMeta meta, final MethodArgumentTypeMismatchException e, final ErrorCode errorCode) {
        final String value = resolveTypeMismatchValue(e);
        log.warn(
                ExceptionLogTemplates.TYPE_MISMATCH_LOG_TEMPLATE.stripTrailing(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                e.getName(),
                value
        );
    }

    public void logMissingParameter(final RequestMeta meta, final MissingServletRequestParameterException e, final ErrorCode errorCode) {
        log.warn(
                ExceptionLogTemplates.MISSING_PARAMETER_LOG_TEMPLATE.stripTrailing(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                e.getParameterName(),
                e.getParameterType()
        );
    }

    public void logMessageOnly(final RequestMeta meta, final ErrorCode errorCode, final String message) {
        final String sanitizedMessage = sanitizeMessage(message);
        log.warn(
                ExceptionLogTemplates.MESSAGE_ONLY_LOG_TEMPLATE.stripTrailing(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                sanitizedMessage
        );
    }

    public void logAccessDenied(final RequestMeta meta, final ErrorCode errorCode, final String message) {
        final String sanitizedMessage = sanitizeMessage(message);
        log.warn(
                ExceptionLogTemplates.ACCESS_DENIED_LOG_TEMPLATE.stripTrailing(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                sanitizedMessage
        );
    }

    public void logUnexpected(final RequestMeta meta, final CurrentAccountDTO account, final Exception e, final ErrorCode errorCode) {
        log.error(
                ExceptionLogTemplates.UNEXPECTED_EXCEPTION_LOG_TEMPLATE.stripTrailing(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                account != null ? account.id() : null,
                account != null ? account.loginId() : null,
                e
        );
    }

    private String resolveErrorName(final ErrorCode errorCode) {
        return errorCode != null ? errorCode.name() : "";
    }

    private String resolveErrorCode(final ErrorCode errorCode) {
        return errorCode != null ? errorCode.getCode() : "";
    }

    private String resolveErrorMessage(final ErrorCode errorCode) {
        return errorCode != null ? errorCode.getErrorMessage() : "";
    }

    private String resolveTypeMismatchValue(final MethodArgumentTypeMismatchException e) {
        if (e == null || e.getName() == null) {
            return null;
        }
        final String raw = e.getValue() != null ? String.valueOf(e.getValue()) : "";
        return LoggingSanitizerPolicy.isSensitiveField(e.getName()) ? "***" : raw;
    }

    private String sanitizeMessage(final String message) {
        final String sanitizedMessage = SensitiveLogMessageSanitizer.sanitize(message);
        return sanitizedMessage != null ? sanitizedMessage : "";
    }
}
