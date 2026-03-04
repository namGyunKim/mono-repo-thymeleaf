package com.example.global.security.handler.support;

import com.example.global.exception.support.FilterLogTemplates;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.LoginLoggingUtils;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 로그인 실패 시 경고 로그를 출력하는 클래스
 */
@Slf4j
@Component
public class LoginFailureLogWriter {

    public void logMissingCredentials(final HttpServletRequest request, final String loginId, final List<ApiErrorDetail> errors) {
        log.warn(
                FilterLogTemplates.LOGIN_MISSING_CREDENTIAL_LOG_TEMPLATE.stripTrailing(),
                ClientIpExtractor.extract(request),
                request != null ? request.getMethod() : "",
                request != null ? request.getRequestURI() : "",
                loginId,
                LoginLoggingUtils.formatErrors(errors)
        );
    }

    public void logAuthFailure(final HttpServletRequest request, final String loginId, final String detailMessage) {
        log.warn(
                FilterLogTemplates.LOGIN_AUTH_FAILURE_LOG_TEMPLATE.stripTrailing(),
                ClientIpExtractor.extract(request),
                request != null ? request.getMethod() : "",
                request != null ? request.getRequestURI() : "",
                loginId,
                detailMessage
        );
    }
}
