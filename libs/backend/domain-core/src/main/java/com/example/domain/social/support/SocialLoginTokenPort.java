package com.example.domain.social.support;

import com.example.domain.account.payload.response.LoginTokenResponse;

/**
 * social 도메인에서 security 도메인의 로그인 토큰 발급 기능을 사용하는 포트.
 *
 * <p>방향: social → security
 *
 * <p>GoogleSocialRedirectCommandService가 LoginTokenCommandService에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface SocialLoginTokenPort {

    /**
     * 소셜 로그인 완료 후 해당 회원에게 액세스/리프레시 토큰 쌍을 발급한다.
     *
     * @param memberId null이 아닌 토큰을 발급할 회원 ID
     * @return 발급된 로그인 토큰 응답 (null이 아님)
     */
    LoginTokenResponse issueTokens(final Long memberId);
}
