package com.example.domain.social.google.payload.dto;

/**
 * 구글 OAuth 로그인 처리 요청 DTO
 */
public record GoogleOauthLoginCommand(String code, String codeVerifier, String expectedNonce) {

    public static GoogleOauthLoginCommand of(final String code, final String codeVerifier, final String expectedNonce) {
        return new GoogleOauthLoginCommand(code, codeVerifier, expectedNonce);
    }

    public static GoogleOauthLoginCommand ofCode(final String code) {
        return of(code, null, null);
    }
}
