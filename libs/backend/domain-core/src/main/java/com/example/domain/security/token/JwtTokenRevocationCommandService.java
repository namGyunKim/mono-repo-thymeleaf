package com.example.domain.security.token;

import com.example.domain.security.port.SecurityMemberTokenPort;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.payload.SecurityLogoutCommand;
import com.example.global.security.RefreshTokenCrypto;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenRevocationCommandService {

    private final BlacklistedTokenCommandService blacklistedTokenCommandService;
    private final SecurityMemberTokenPort securityMemberTokenPort;
    private final RefreshTokenCrypto refreshTokenCrypto;

    public void revokeOnLogout(final SecurityLogoutCommand command) {
        if (command == null) {
            return;
        }

        revokeMemberTokensById(command.memberId(), command.accessToken());
    }

    private void revokeMemberTokensById(final Long memberId, final String accessToken) {
        if (memberId == null || memberId <= 0) {
            return;
        }

        if (StringUtils.hasText(accessToken)) {
            blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(accessToken));
            log.info("액세스 토큰 블랙리스트 등록 완료: memberId={}", memberId);
        }

        blacklistStoredRefreshToken(memberId);
        securityMemberTokenPort.revokeTokens(memberId);
        log.info("리프레시 토큰 폐기 완료: memberId={}", memberId);
    }

    private void blacklistStoredRefreshToken(final Long memberId) {
        securityMemberTokenPort.findTokenInfoById(memberId)
                .ifPresent(memberInfo -> {
                    final String encrypted = memberInfo.refreshTokenEncrypted();
                    if (!StringUtils.hasText(encrypted)) {
                        return;
                    }

                    try {
                        final String storedRefreshToken = refreshTokenCrypto.decrypt(encrypted);
                        if (StringUtils.hasText(storedRefreshToken)) {
                            blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(storedRefreshToken));
                            log.info("저장된 리프레시 토큰 블랙리스트 등록 완료: memberId={}", memberId);
                        }
                    } catch (final IllegalStateException e) {
                        log.warn("리프레시 토큰 복호화 실패로 블랙리스트 등록 생략: memberId={}", memberId, e);
                    }
                });
    }
}
