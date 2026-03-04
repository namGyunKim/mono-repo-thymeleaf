package com.example.global.security.blacklist.payload.dto;

/**
 * 블랙리스트 토큰 등록용 Command DTO
 */
public record BlacklistedTokenRegisterCommand(
        String token
) {

    public static BlacklistedTokenRegisterCommand of(final String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token은 필수입니다.");
        }
        return new BlacklistedTokenRegisterCommand(token);
    }
}
