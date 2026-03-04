package com.example.domain.security.token;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.response.RefreshTokenResponse;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;
import com.example.domain.security.jwt.JwtTokenParser;
import com.example.domain.security.jwt.JwtTokenParseResult;
import com.example.domain.security.jwt.JwtTokenPayload;
import com.example.domain.security.port.SecurityMemberTokenInfo;
import com.example.domain.security.port.SecurityMemberTokenPort;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.blacklist.support.BlacklistedTokenChecker;
import com.example.global.security.jwt.JwtTokenParseStatus;
import com.example.global.security.jwt.JwtTokenType;
import com.example.global.security.payload.RefreshTokenIssueCommand;
import com.example.global.security.RefreshTokenCrypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

import java.time.Instant;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JwtTokenRefreshCommandServiceTest {

    @InjectMocks
    private JwtTokenRefreshCommandService jwtTokenRefreshCommandService;

    @Mock
    private JwtTokenCommandService jwtTokenCommandService;

    @Mock
    private JwtTokenParser jwtTokenParser;

    @Mock
    private BlacklistedTokenCommandService blacklistedTokenCommandService;

    @Mock
    private BlacklistedTokenChecker blacklistedTokenChecker;

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

    private JwtTokenPayload createRefreshPayload() {
        return JwtTokenPayload.of(
                "testUser", AccountRole.USER, JwtTokenType.REFRESH,
                1L, Instant.now(), Instant.now().plusSeconds(3600)
        );
    }

    private void stubSuccessfulRefreshFlow(final String refreshToken, final String encryptedRefreshToken) {
        final JwtTokenPayload payload = createRefreshPayload();
        final SecurityMemberTokenInfo memberInfo = createMemberTokenInfo(encryptedRefreshToken);

        given(jwtTokenParser.parseTokenResult(refreshToken))
                .willReturn(JwtTokenParseResult.of(JwtTokenParseStatus.VALID, payload));
        given(securityMemberTokenPort.findTokenInfoByLoginId("testUser"))
                .willReturn(Optional.of(memberInfo));
        given(blacklistedTokenChecker.isBlacklisted(refreshToken))
                .willReturn(false);
        given(refreshTokenCrypto.decrypt(encryptedRefreshToken))
                .willReturn(refreshToken);
        given(jwtTokenCommandService.generateAccessToken(memberInfo))
                .willReturn("new-access-token");
        given(jwtTokenCommandService.generateRefreshToken(memberInfo))
                .willReturn("new-refresh-token");
        given(refreshTokenCrypto.encrypt("new-refresh-token"))
                .willReturn("new-encrypted-refresh-token");
    }

    @Test
    @DisplayName("재발급 시 기존 액세스 토큰이 있으면 블랙리스트에 등록한다")
    void refreshTokens_blacklists_old_access_token() {
        // Arrange
        final String refreshToken = "valid-refresh-token";
        final String oldAccessToken = "old-access-token";
        final String encryptedRefreshToken = "encrypted-refresh-token";

        stubSuccessfulRefreshFlow(refreshToken, encryptedRefreshToken);

        final RefreshTokenIssueCommand command = RefreshTokenIssueCommand.of(refreshToken, oldAccessToken);

        // Act
        final RefreshTokenResponse response = jwtTokenRefreshCommandService.refreshTokens(command);

        // Assert — 기존 액세스 토큰 + 기존 리프레시 토큰 2회 블랙리스트 등록
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");

        verify(blacklistedTokenCommandService, times(2)).blacklistToken(any(BlacklistedTokenRegisterCommand.class));
        verify(blacklistedTokenCommandService).blacklistToken(argThat(cmd -> oldAccessToken.equals(cmd.token())));
        verify(blacklistedTokenCommandService).blacklistToken(argThat(cmd -> refreshToken.equals(cmd.token())));
    }

    @Test
    @DisplayName("재발급 시 기존 액세스 토큰이 없으면 리프레시 토큰만 블랙리스트에 등록한다")
    void refreshTokens_without_access_token_blacklists_only_refresh() {
        // Arrange
        final String refreshToken = "valid-refresh-token";
        final String encryptedRefreshToken = "encrypted-refresh-token";

        stubSuccessfulRefreshFlow(refreshToken, encryptedRefreshToken);

        final RefreshTokenIssueCommand command = RefreshTokenIssueCommand.of(refreshToken);

        // Act
        final RefreshTokenResponse response = jwtTokenRefreshCommandService.refreshTokens(command);

        // Assert — 리프레시 토큰만 블랙리스트 등록
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");

        verify(blacklistedTokenCommandService, times(1)).blacklistToken(any(BlacklistedTokenRegisterCommand.class));
        verify(blacklistedTokenCommandService).blacklistToken(argThat(cmd -> refreshToken.equals(cmd.token())));
    }
}
