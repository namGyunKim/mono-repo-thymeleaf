package com.example.global.security.support;

import com.example.global.security.SecurityHeaders;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalTokenHeaderLoggingSupport {

    private final Environment environment;

    public void logResponseTokenHeaders(final String flow, final String accessToken, final String refreshToken) {
        if (!isLocalProfileActive()) {
            return;
        }

        final String authorizationHeaderValue = SecurityHeaders.BEARER_PREFIX + accessToken;
        final String refreshHeaderValue = refreshToken;
        log.info(
                """
                        flow={}, local 프로필 토큰 헤더 로그
                        {}={}
                        {}={}
                        """.stripTrailing(),
                flow,
                SecurityHeaders.AUTHORIZATION,
                authorizationHeaderValue,
                SecurityHeaders.REFRESH_TOKEN,
                refreshHeaderValue
        );
    }

    private boolean isLocalProfileActive() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch("local"::equalsIgnoreCase);
    }
}
