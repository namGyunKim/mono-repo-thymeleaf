package com.example.domain.log.support;

/**
 * log 도메인에서 security 도메인의 인증 상태를 확인하는 포트.
 *
 * <p>방향: log → security
 *
 * <p>ExceptionMemberActivityEventListener가 MemberGuard에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface LogAuthenticationCheckPort {

    /**
     * 현재 요청의 인증 상태를 확인한다.
     *
     * <p>예외 발생 시 활동 로그 기록 여부를 결정하기 위해 사용한다.
     * 인증되지 않은 요청에서 발생한 예외는 활동 로그에 기록하지 않을 수 있다.
     *
     * @return 현재 요청이 인증된 상태이면 {@code true}
     */
    boolean isAuthenticated();
}
