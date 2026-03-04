package com.example.global.security.handler;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.security.guard.MemberGuard;
import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.support.SecurityJsonResponseWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 접근 거부(403) 처리 핸들러
 * <p>
 * - REST API 전용으로 JSON 응답을 반환합니다.
 * - 기존 ExceptionAdvice에서 수행하던 예외 로깅(Event 발행) 흐름을 유지합니다.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberGuard memberGuard;

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException accessDeniedException) throws IOException {
        publishAccessDeniedEvent(request, accessDeniedException, resolveMessage(accessDeniedException));

        if (response.isCommitted()) {
            return;
        }

        if (request != null) {
            request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
        }

        if (isApiRequest(request)) {
            final ApiErrorResponse body = ApiErrorResponse.from(ErrorCode.ACCESS_DENIED);
            SecurityJsonResponseWriter.writeJsonErrorResponse(response, HttpStatus.FORBIDDEN.value(), body, objectMapper);
        } else {
            response.sendRedirect("/error/403");
        }
    }

    private void publishAccessDeniedEvent(final HttpServletRequest request, final AccessDeniedException e, final String message) {
        if (request == null || e == null) {
            return;
        }

        final CurrentAccountDTO currentAccount = resolveCurrentAccount();
        applicationEventPublisher.publishEvent(
                ExceptionEvent.from(e, ErrorCode.ACCESS_DENIED, message, currentAccount, request)
        );
    }

    private CurrentAccountDTO resolveCurrentAccount() {
        return memberGuard.getCurrentAccount().orElse(null);
    }

    private boolean isApiRequest(final HttpServletRequest request) {
        if (request == null) {
            return true;
        }
        final String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/");
    }

    private String resolveMessage(final AccessDeniedException e) {
        if (e == null) {
            return "권한이 없습니다.";
        }
        final String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return "권한이 없습니다.";
        }
        return msg;
    }

}
