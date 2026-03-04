package com.example.global.security.blacklist.payload.dto;

/**
 * 블랙리스트 여부 확인용 Query DTO
 */
public record BlacklistedTokenCheckQuery(
        String token
) {

    public static BlacklistedTokenCheckQuery of(final String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token은 필수입니다.");
        }
        return new BlacklistedTokenCheckQuery(token);
    }
}
