package com.example.domain.social.google.payload.dto;

import com.example.domain.social.google.payload.request.GoogleRedirectRequest;

/**
 * 구글 OAuth 리다이렉트 콜백 처리 요청 커맨드
 */
public record GoogleSocialRedirectCommand(
        String code,
        String state,
        String error,
        String errorDescription
) {

    public static GoogleSocialRedirectCommand from(final GoogleRedirectRequest request) {
        if (request == null) {
            return of(null, null, null, null);
        }
        return of(request.code(), request.state(), request.error(), request.errorDescription());
    }

    public static GoogleSocialRedirectCommand of(final String code, final String state, final String error, final String errorDescription) {
        return new GoogleSocialRedirectCommand(code, state, error, errorDescription);
    }
}
