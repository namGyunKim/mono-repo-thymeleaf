package com.example.global.security.payload;

/**
 * 리프레시 토큰 갱신 요청 DTO
 *
 * @param refreshToken 리프레시 토큰 (필수)
 * @param accessToken  기존 액세스 토큰 (nullable, 블랙리스트 등록용)
 */
public record RefreshTokenIssueCommand(
        String refreshToken,
        String accessToken
) {

    /**
     * 리프레시 토큰만으로 생성한다.
     */
    public static RefreshTokenIssueCommand of(final String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken은 필수입니다.");
        }
        return new RefreshTokenIssueCommand(refreshToken, null);
    }

    /**
     * 리프레시 토큰과 기존 액세스 토큰으로 생성한다.
     */
    public static RefreshTokenIssueCommand of(final String refreshToken, final String accessToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken은 필수입니다.");
        }
        return new RefreshTokenIssueCommand(refreshToken, accessToken);
    }
}
