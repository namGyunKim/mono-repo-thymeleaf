package com.example.domain.security.jwt;

import com.example.global.security.jwt.JwtTokenParseStatus;

import java.util.Objects;
import java.util.Optional;

public record JwtTokenParseResult(
        JwtTokenParseStatus status,
        JwtTokenPayload payload
) {

    public JwtTokenParseResult {
        status = Objects.requireNonNullElse(status, JwtTokenParseStatus.INVALID);
        if (status == JwtTokenParseStatus.VALID && payload == null) {
            status = JwtTokenParseStatus.INVALID;
        }
        if (status != JwtTokenParseStatus.VALID) {
            payload = null;
        }
    }

    public static JwtTokenParseResult of(final JwtTokenParseStatus status, final JwtTokenPayload payload) {
        return new JwtTokenParseResult(status, payload);
    }

    public Optional<JwtTokenPayload> payloadOptional() {
        return Optional.ofNullable(payload);
    }
}
