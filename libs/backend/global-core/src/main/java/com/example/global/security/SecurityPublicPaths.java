package com.example.global.security;

import org.springframework.util.StringUtils;

/**
 * 보안 공개 경로 정책 상수
 *
 * <p>
 * - SecurityConfig가 참조하는 공개 경로 기준을 관리합니다.
 * </p>
 */
public final class SecurityPublicPaths {

    public static final String[] PUBLIC_URLS = {
            "/",
            "/register",
            "/favicon.ico",
            "/favicon.svg",
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**"
    };

    public static final String[] PUBLIC_API_URLS = {
            "/api/health",
            "/api/sessions",
            "/api/social/**"
    };

    private static final String HEALTH_CHECK_PATH = "/api/health";
    private static final String LOGIN_PATH = "/api/sessions";
    private static final String SOCIAL_API_BASE_PATH = "/api/social";

    private SecurityPublicPaths() {
    }

    public static boolean isPublicApiPath(final String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }

        final String normalizedPath = path.trim();
        if (HEALTH_CHECK_PATH.equals(normalizedPath)) {
            return true;
        }
        if (LOGIN_PATH.equals(normalizedPath)) {
            return true;
        }
        return SOCIAL_API_BASE_PATH.equals(normalizedPath)
                || normalizedPath.startsWith(SOCIAL_API_BASE_PATH + "/");
    }
}
