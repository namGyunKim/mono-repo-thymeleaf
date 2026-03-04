package com.example.domain.social.google.payload.dto;

import com.example.domain.social.google.payload.response.GoogleUserInfoResponse;

/**
 * 구글 OAuth 처리 결과 DTO
 */
public record GoogleOauthResult(
        GoogleUserInfoResponse userInfo,
        String refreshToken
) {

    public static GoogleOauthResult of(final GoogleUserInfoResponse userInfo, final String refreshToken) {
        return new GoogleOauthResult(userInfo, refreshToken);
    }
}
