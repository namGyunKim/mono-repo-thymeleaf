package com.example.domain.security.port;

import com.example.domain.log.enums.LogType;

/**
 * Security 도메인(및 security-web) → log 도메인 경계를 넘는 로그인 활동 로그 발행 포트
 *
 * <p>
 * LoginSuccessEventPublisher / LoginFailureEventPublisher가 LogActivityPublisher에
 * 직접 의존하지 않도록 추상화합니다.
 * </p>
 */
public interface SecurityLoginActivityPublishPort {

    /**
     * 로그인 성공/실패 활동 로그를 발행한다.
     *
     * <p>로그인 이벤트 리스너에서 호출되어 log 도메인으로 활동 기록을 전달한다.
     *
     * @param loginId  null이 아닌 비어 있지 않은 대상 회원의 로그인 ID
     * @param memberId null이 아닌 대상 회원 ID
     * @param logType  null이 아닌 활동 로그 유형 (LOGIN_SUCCESS, LOGIN_FAILURE 등)
     * @param details  null이 아닌 활동 상세 내용 (빈 문자열 허용)
     */
    void publishMemberActivity(final String loginId, final Long memberId, final LogType logType, final String details);
}
