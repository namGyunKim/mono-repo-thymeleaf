package com.example.global.security.handler;

import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.FilterLogTemplates;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.support.SecurityJsonResponseWriter;
import com.example.global.utils.ClientIpExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 인증 실패(401) 처리 EntryPoint
 * <p>
 * - API 요청: JSON 에러 응답을 반환합니다.
 * - 페이지 요청: 로그인 페이지로 리다이렉트합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        if (request != null) {
            request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
        }

        log.warn(
                FilterLogTemplates.AUTHENTICATION_ENTRYPOINT_LOG_TEMPLATE.stripTrailing(),
                ClientIpExtractor.extract(request),
                request != null ? request.getMethod() : "",
                request != null ? request.getRequestURI() : "",
                ErrorCode.AUTHENTICATION_REQUIRED.name(),
                ErrorCode.AUTHENTICATION_REQUIRED.getCode(),
                ErrorCode.AUTHENTICATION_REQUIRED.getErrorMessage(),
                authException != null ? authException.getMessage() : ""
        );

        if (isApiRequest(request)) {
            final ApiErrorResponse body = ApiErrorResponse.from(ErrorCode.AUTHENTICATION_REQUIRED);
            SecurityJsonResponseWriter.writeJsonErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), body, objectMapper);
        } else {
            response.sendRedirect("/");
        }
    }

    private boolean isApiRequest(final HttpServletRequest request) {
        if (request == null) {
            return true;
        }
        final String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/api/");
    }
}
