package com.example.domain.account.support;

import com.example.domain.account.payload.dto.AccountActivityPublishCommand;

/**
 * account 도메인에서 log 도메인으로 활동 로그를 발행하는 포트.
 *
 * <p>방향: account → log
 *
 * <p>AccountCommandService가 LogActivityPublisher / MemberActivityCommand에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface AccountActivityPublishPort {

    /**
     * 회원 활동 로그를 발행한다.
     *
     * <p>로그인, 로그아웃, 프로필 수정, 탈퇴 등의 활동을 기록할 때 사용한다.
     *
     * @param command null이 아닌 활동 로그 발행 커맨드.
     *                loginId, memberId, logType, details를 포함해야 한다.
     */
    void publishMemberActivity(final AccountActivityPublishCommand command);
}
