package com.example.global.security.handler.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.security.guard.PrincipalDetails;
import com.example.domain.security.port.SecurityLoginActivityPublishPort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 로그인 성공 이벤트를 발행하여 활동 로그를 기록하는 클래스
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessEventPublisher {

    private static final String DEFAULT_MESSAGE = "로그인 성공";

    private final SecurityLoginActivityPublishPort activityPublishPort;

    public void publish(final PrincipalDetails principal, final String message) {
        if (principal == null) {
            return;
        }

        final String resolvedMessage = (message == null || message.isBlank()) ? DEFAULT_MESSAGE : message;
        activityPublishPort.publishMemberActivity(
                principal.getUsername(),
                principal.getId(),
                LogType.LOGIN,
                resolvedMessage
        );
    }
}
