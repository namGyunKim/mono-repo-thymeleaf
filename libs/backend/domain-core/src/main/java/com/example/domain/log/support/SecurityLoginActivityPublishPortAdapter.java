package com.example.domain.log.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.LogActivityPublisher;
import com.example.domain.security.port.SecurityLoginActivityPublishPort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityLoginActivityPublishPortAdapter implements SecurityLoginActivityPublishPort {

    private final LogActivityPublisher activityEventPublisher;

    @Override
    public void publishMemberActivity(final String loginId, final Long memberId, final LogType logType, final String details) {
        activityEventPublisher.publishMemberActivity(MemberActivityCommand.of(loginId, memberId, logType, details));
    }
}
