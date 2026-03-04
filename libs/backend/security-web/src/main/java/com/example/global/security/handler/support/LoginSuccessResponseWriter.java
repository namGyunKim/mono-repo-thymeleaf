package com.example.global.security.handler.support;

import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.global.security.SecurityHeaders;
import com.example.global.security.support.LocalTokenHeaderLoggingSupport;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * 로그인 성공 시 JWT 토큰을 응답 헤더에 설정하는 클래스
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessResponseWriter {

    private final LocalTokenHeaderLoggingSupport localTokenHeaderLoggingSupport;

    public void writeSuccess(final HttpServletResponse response, final LoginTokenResponse loginTokenResponse) {
        if (response == null || response.isCommitted() || loginTokenResponse == null) {
            return;
        }

        localTokenHeaderLoggingSupport.logResponseTokenHeaders(
                "login",
                loginTokenResponse.accessToken(),
                loginTokenResponse.refreshToken()
        );
        response.setHeader(
                SecurityHeaders.AUTHORIZATION,
                SecurityHeaders.BEARER_PREFIX + loginTokenResponse.accessToken()
        );
        response.setHeader(SecurityHeaders.REFRESH_TOKEN, loginTokenResponse.refreshToken());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
}
