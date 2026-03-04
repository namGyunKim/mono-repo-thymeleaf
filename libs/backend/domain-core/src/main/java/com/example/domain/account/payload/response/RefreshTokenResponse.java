package com.example.domain.account.payload.response;

/**
 * 리프레시 토큰 갱신 응답
 */
public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {

    public static RefreshTokenResponse of(final String accessToken, final String refreshToken) {
        return new RefreshTokenResponse(accessToken, refreshToken);
    }
}
