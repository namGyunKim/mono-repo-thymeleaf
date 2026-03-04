package com.example.domain.social.payload.response;

import java.util.Objects;

public record SocialLoginSuccessResponse(
        String status,
        String message
) {
    public SocialLoginSuccessResponse {
        status = Objects.requireNonNullElse(status, "");
        message = Objects.requireNonNullElse(message, "");
    }

    public static SocialLoginSuccessResponse of(final String status, final String message) {
        return new SocialLoginSuccessResponse(status, message);
    }

    public static SocialLoginSuccessResponse ok() {
        return of("OK", "소셜 로그인이 완료되었습니다.");
    }
}
