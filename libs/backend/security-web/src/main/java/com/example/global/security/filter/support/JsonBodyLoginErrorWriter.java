package com.example.global.security.filter.support;

import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.FilterLogTemplates;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.filter.JsonBodyLoginAuthenticationFilter;
import com.example.global.security.support.SecurityJsonResponseWriter;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.LoginLoggingUtils;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonBodyLoginErrorWriter {

    private final ObjectMapper objectMapper;

    public void writeBadRequest(final HttpServletRequest request, final HttpServletResponse response, final ErrorCode errorCode, final List<ApiErrorDetail> errors) {
        if (response == null || response.isCommitted()) {
            return;
        }

        markFilterLogged(request);

        logBadRequest(request, errorCode, errors);

        final String traceId = TraceIdUtils.resolveTraceId();
        writeJsonResponse(response, traceId, errorCode, errors);
    }

    private void markFilterLogged(final HttpServletRequest request) {
        if (request != null) {
            request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
        }
    }

    private void logBadRequest(final HttpServletRequest request, final ErrorCode errorCode, final List<ApiErrorDetail> errors) {
        final String ip = ClientIpExtractor.extract(request);
        final String method = LoginLoggingUtils.safe(request != null ? request.getMethod() : null);
        final String uri = LoginLoggingUtils.safe(request != null ? request.getRequestURI() : null);
        final String loginId = LoginLoggingUtils.resolveLoginIdOrDefault(
                request,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID,
                LoginLoggingUtils.DEFAULT_UNKNOWN_LOGIN_ID
        );

        final String code = errorCode != null ? errorCode.getCode() : "";
        final String message = errorCode != null ? errorCode.getErrorMessage() : "";
        final String errorName = errorCode != null ? errorCode.name() : "";
        final String formattedErrors = LoginLoggingUtils.formatErrors(errors);

        log.warn(
                FilterLogTemplates.LOGIN_JSON_BAD_REQUEST_LOG_TEMPLATE.stripTrailing(),
                ip, method, uri, loginId,
                errorName, code, message, formattedErrors
        );
    }

    private void writeJsonResponse(final HttpServletResponse response, final String traceId, final ErrorCode errorCode, final List<ApiErrorDetail> errors) {
        final String code = errorCode != null ? errorCode.getCode() : "";
        final String message = errorCode != null ? errorCode.getErrorMessage() : "";

        try {
            final ApiErrorResponse body = ApiErrorResponse.of(code, message, traceId, errors);
            SecurityJsonResponseWriter.writeJsonErrorResponse(response, HttpStatus.BAD_REQUEST.value(), body, objectMapper);
        } catch (final IOException e) {
            log.warn("응답 쓰기 실패", e);
        }
    }
}
