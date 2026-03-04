package com.example.global.config.web.support;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.security.guard.MemberGuard;
import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogWriter;
import com.example.global.payload.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ApiVersionErrorResponder {

    private final ObjectMapper objectMapper;
    private final ExceptionLogWriter exceptionLogWriter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberGuard memberGuard;

    public void writeErrorResponse(final HttpServletRequest request, final HttpServletResponse response, final ErrorCode errorCode) throws IOException {
        if (response == null || response.isCommitted()) {
            return;
        }

        markFilterLogged(request);
        logVersionError(request, errorCode);
        publishExceptionEvent(request, errorCode);

        final ApiErrorResponse body = ApiErrorResponse.from(errorCode);
        sendJsonResponse(response, body);
    }

    /**
     * HTTP 응답에 JSON 형식의 에러 본문을 기록한다.
     */
    private void sendJsonResponse(final HttpServletResponse response, final ApiErrorResponse body) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void logVersionError(final HttpServletRequest request, final ErrorCode errorCode) {
        if (request == null || errorCode == null) {
            return;
        }

        exceptionLogWriter.logMessageOnly(
                exceptionLogWriter.resolveRequestMeta(request),
                errorCode,
                errorCode.getErrorMessage()
        );
    }

    private void publishExceptionEvent(final HttpServletRequest request, final ErrorCode errorCode) {
        if (request == null || errorCode == null) {
            return;
        }

        final CurrentAccountDTO account = memberGuard.getCurrentAccountOrGuest();
        final String detailMessage = errorCode.getErrorMessage();

        applicationEventPublisher.publishEvent(ExceptionEvent.from(
                new IllegalArgumentException(detailMessage),
                errorCode,
                detailMessage,
                account,
                request
        ));
    }

    private void markFilterLogged(final HttpServletRequest request) {
        if (request == null) {
            return;
        }
        request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
    }
}
