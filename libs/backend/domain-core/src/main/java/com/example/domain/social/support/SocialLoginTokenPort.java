package com.example.domain.social.support;

/**
 * social 도메인에서 security 도메인의 세션 인증 기능을 사용하는 포트.
 *
 * <p>방향: social → security
 *
 * <p>GoogleSocialRedirectCommandService가 security 도메인에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface SocialLoginTokenPort {

    /**
     * 소셜 로그인 완료 후 해당 회원에 대한 인증 세션을 설정한다.
     *
     * @param memberId null이 아닌 세션을 설정할 회원 ID
     */
    void authenticateBySession(final Long memberId);
}
