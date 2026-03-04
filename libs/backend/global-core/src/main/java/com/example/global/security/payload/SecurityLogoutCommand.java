package com.example.global.security.payload;

public record SecurityLogoutCommand(
        Long memberId,
        String accessToken
) {
    public static SecurityLogoutCommand of(final Long memberId, final String accessToken) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("accessToken은 필수입니다.");
        }
        return new SecurityLogoutCommand(memberId, accessToken);
    }
}
