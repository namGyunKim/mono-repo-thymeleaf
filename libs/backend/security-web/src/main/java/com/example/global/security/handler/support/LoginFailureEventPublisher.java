package com.example.global.security.handler.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.security.port.SecurityLoginActivityPublishPort;
import com.example.global.utils.LoginLoggingUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 로그인 실패 이벤트를 발행하여 활동 로그를 기록하는 클래스
 */
@Component
@RequiredArgsConstructor
public class LoginFailureEventPublisher {

    private final SecurityLoginActivityPublishPort activityPublishPort;

    public void publishLoginFailEvent(final String loginId, final Long memberId, final String detailMessage) {
        final String resolvedLoginId = StringUtils.hasText(loginId) ? loginId : LoginLoggingUtils.DEFAULT_UNKNOWN_LOGIN_ID;
        activityPublishPort.publishMemberActivity(
                resolvedLoginId,
                memberId,
                LogType.LOGIN_FAIL,
                detailMessage != null ? detailMessage : "로그인 실패"
        );
    }
}
