package com.example.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그아웃 성공 핸들러
 * <p>
 * - API 요청: 204 No Content 응답
 * - 페이지 요청: 로그인 페이지로 리다이렉트
 * </p>
 */
@Component
public class RoleBasedLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException {
        if (isApiRequest(request)) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            response.sendRedirect("/login?logout");
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
