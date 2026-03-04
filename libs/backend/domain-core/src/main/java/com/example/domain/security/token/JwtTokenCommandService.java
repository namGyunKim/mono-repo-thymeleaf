package com.example.domain.security.token;

import com.example.domain.security.port.SecurityMemberTokenInfo;
import com.example.global.security.jwt.JwtTokenClaimKeys;
import com.example.global.security.jwt.JwtTokenKeyProvider;
import com.example.global.security.jwt.JwtTokenType;

import io.jsonwebtoken.Jwts;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JwtTokenCommandService {

    private final JwtTokenKeyProvider keyProvider;

    public String generateAccessToken(final SecurityMemberTokenInfo memberInfo) {
        return generateToken(memberInfo, JwtTokenType.ACCESS, keyProvider.getProperties().accessTokenTtl());
    }

    public String generateRefreshToken(final SecurityMemberTokenInfo memberInfo) {
        return generateToken(memberInfo, JwtTokenType.REFRESH, keyProvider.getProperties().refreshTokenTtl());
    }

    private String generateToken(final SecurityMemberTokenInfo memberInfo, final JwtTokenType tokenType, final Duration ttl) {
        validateTtl(ttl, tokenType.name().toLowerCase());
        final Instant now = Instant.now();
        final Instant expiresAt = now.plus(ttl);

        return Jwts.builder()
                .issuer(keyProvider.getProperties().issuer())
                .subject(memberInfo.loginId())
                .claim(JwtTokenClaimKeys.ROLE, memberInfo.role().name())
                .claim(JwtTokenClaimKeys.TYPE, tokenType.name())
                .claim(JwtTokenClaimKeys.VERSION, memberInfo.tokenVersion())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(keyProvider.getSecretKey())
                .compact();
    }

    private void validateTtl(final Duration ttl, final String tokenLabel) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("JWT %s TTL은 0보다 커야 합니다.".formatted(tokenLabel));
        }
    }
}
