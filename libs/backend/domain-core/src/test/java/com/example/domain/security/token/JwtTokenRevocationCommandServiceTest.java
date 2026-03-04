package com.example.domain.security.token;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;
import com.example.domain.security.port.SecurityMemberTokenInfo;
import com.example.domain.security.port.SecurityMemberTokenPort;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.payload.SecurityLogoutCommand;
import com.example.global.security.RefreshTokenCrypto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JwtTokenRevocationCommandServiceTest {

    @InjectMocks
    private JwtTokenRevocationCommandService jwtTokenRevocationCommandService;

    @Mock
    private BlacklistedTokenCommandService blacklistedTokenCommandService;

    @Mock
    private SecurityMemberTokenPort securityMemberTokenPort;

    @Mock
    private RefreshTokenCrypto refreshTokenCrypto;

    private SecurityMemberTokenInfo createMemberTokenInfo(final String refreshTokenEncrypted) {
        return SecurityMemberTokenInfo.of(
                1L, "testUser", AccountRole.USER, "testNick",
                MemberType.GENERAL, MemberActiveStatus.ACTIVE, 1L,
                refreshTokenEncrypted
        );
    }

    @Test
    @DisplayName("로그아웃 시 저장된 리프레시 토큰도 블랙리스트에 등록한다")
    void revokeOnLogout_blacklists_stored_refresh_token() {
        // Arrange
        final String accessToken = "access-token-value";
        final String encryptedRefreshToken = "encrypted-refresh-token";
        final String decryptedRefreshToken = "decrypted-refresh-token";
        final SecurityLogoutCommand command = SecurityLogoutCommand.of(1L, accessToken);

        given(securityMemberTokenPort.findTokenInfoById(1L))
                .willReturn(Optional.of(createMemberTokenInfo(encryptedRefreshToken)));
        given(refreshTokenCrypto.decrypt(encryptedRefreshToken))
                .willReturn(decryptedRefreshToken);

        // Act
        jwtTokenRevocationCommandService.revokeOnLogout(command);

        // Assert — 액세스 토큰 + 리프레시 토큰 2회 블랙리스트 등록
        verify(blacklistedTokenCommandService, times(2)).blacklistToken(any(BlacklistedTokenRegisterCommand.class));
        verify(blacklistedTokenCommandService).blacklistToken(argThat(cmd -> accessToken.equals(cmd.token())));
        verify(blacklistedTokenCommandService).blacklistToken(argThat(cmd -> decryptedRefreshToken.equals(cmd.token())));
        verify(securityMemberTokenPort).revokeTokens(1L);
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰 복호화 실패하면 블랙리스트 등록을 생략하고 DB 폐기는 진행한다")
    void revokeOnLogout_skips_refresh_blacklist_on_decrypt_failure() {
        // Arrange
        final String accessToken = "access-token-value";
        final String encryptedRefreshToken = "encrypted-refresh-token";
        final SecurityLogoutCommand command = SecurityLogoutCommand.of(1L, accessToken);

        given(securityMemberTokenPort.findTokenInfoById(1L))
                .willReturn(Optional.of(createMemberTokenInfo(encryptedRefreshToken)));
        given(refreshTokenCrypto.decrypt(encryptedRefreshToken))
                .willThrow(new IllegalStateException("복호화 실패"));

        // Act
        jwtTokenRevocationCommandService.revokeOnLogout(command);

        // Assert — 액세스 토큰만 블랙리스트 등록, DB 폐기는 정상 진행
        verify(blacklistedTokenCommandService, times(1)).blacklistToken(any(BlacklistedTokenRegisterCommand.class));
        verify(blacklistedTokenCommandService).blacklistToken(argThat(cmd -> accessToken.equals(cmd.token())));
        verify(securityMemberTokenPort).revokeTokens(1L);
    }

    @Test
    @DisplayName("로그아웃 시 저장된 리프레시 토큰이 없으면 블랙리스트 등록을 생략한다")
    void revokeOnLogout_skips_when_no_stored_refresh_token() {
        // Arrange
        final String accessToken = "access-token-value";
        final SecurityLogoutCommand command = SecurityLogoutCommand.of(1L, accessToken);

        given(securityMemberTokenPort.findTokenInfoById(1L))
                .willReturn(Optional.of(createMemberTokenInfo(null)));

        // Act
        jwtTokenRevocationCommandService.revokeOnLogout(command);

        // Assert — 액세스 토큰만 블랙리스트 등록, 리프레시 토큰 복호화 시도 없음
        verify(blacklistedTokenCommandService, times(1)).blacklistToken(any(BlacklistedTokenRegisterCommand.class));
        verify(refreshTokenCrypto, never()).decrypt(any());
        verify(securityMemberTokenPort).revokeTokens(1L);
    }

    @Test
    @DisplayName("null command는 아무 동작 없이 반환한다")
    void revokeOnLogout_null_command_does_nothing() {
        // Act
        jwtTokenRevocationCommandService.revokeOnLogout(null);

        // Assert
        verify(blacklistedTokenCommandService, never()).blacklistToken(any());
        verify(securityMemberTokenPort, never()).revokeTokens(any());
    }
}
