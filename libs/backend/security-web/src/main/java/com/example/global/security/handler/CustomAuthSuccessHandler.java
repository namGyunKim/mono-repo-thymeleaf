package com.example.global.security.handler;

import com.example.domain.security.guard.PrincipalDetails;
import com.example.global.security.handler.support.LoginSuccessEventPublisher;
import com.example.global.security.handler.support.LoginSuccessMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginSuccessEventPublisher loginSuccessEventPublisher;
    private final LoginSuccessMessageResolver loginSuccessMessageResolver;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) {
        final PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        final String message = loginSuccessMessageResolver.resolve(request);
        loginSuccessEventPublisher.publish(principal, message);

        // 세션 기반 인증: SecurityContext는 Spring Security가 자동으로 세션에 저장합니다.
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
}
