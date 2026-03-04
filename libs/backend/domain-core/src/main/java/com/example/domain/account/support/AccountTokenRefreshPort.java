package com.example.domain.account.support;

import com.example.domain.account.payload.response.RefreshTokenResponse;

/**
 * account 도메인에서 security 도메인의 토큰 갱신 기능을 사용하는 포트.
 *
 * <p>방향: account → security
 *
 * <p>AccountAuthApiController가 JwtTokenRefreshCommandService에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface AccountTokenRefreshPort {

    /**
     * 리프레시 토큰을 검증하고 새로운 액세스/리프레시 토큰 쌍을 발급한다.
     *
     * @param refreshToken null이 아닌 비어 있지 않은 리프레시 토큰 문자열
     * @param accessToken  기존 액세스 토큰 (nullable, 블랙리스트 등록용)
     * @return 새로 발급된 토큰 쌍을 담은 응답 (null이 아님)
     * @throws com.example.global.exception.GlobalException 리프레시 토큰이 만료되었거나 유효하지 않은 경우
     */
    RefreshTokenResponse refreshTokens(final String refreshToken, final String accessToken);
}
