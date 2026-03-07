package com.example.domain.social.google.service.command;

import com.example.domain.social.google.payload.dto.GoogleOauthLoginCommand;
import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.payload.dto.GoogleSocialRedirectCommand;
import com.example.domain.social.google.support.GoogleOauthSessionResolver;
import com.example.domain.social.support.SocialLoginTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoogleSocialRedirectCommandServiceTest {

    @InjectMocks
    private GoogleSocialRedirectCommandService googleSocialRedirectCommandService;

    @Mock
    private GoogleOauthSessionResolver googleOauthSessionResolver;

    @Mock
    private GoogleSocialCommandService googleSocialCommandService;

    @Mock
    private SocialLoginTokenPort socialLoginTokenPort;

    @Test
    @DisplayName("loginByRedirect는 세션 검증 후 소셜 로그인 처리하고 세션 인증을 수행한다")
    void loginByRedirect_delegates_to_resolver_and_authenticates_by_session() {
        // Arrange
        final GoogleSocialRedirectCommand command = GoogleSocialRedirectCommand.of(
                "auth-code", "state-value", null, null
        );
        final GoogleOauthSession oauthSession = GoogleOauthSession.of(
                "state-value", "nonce", "codeVerifier", "codeChallenge", System.currentTimeMillis()
        );
        final Long memberId = 42L;

        given(googleOauthSessionResolver.resolveAndConsume(command)).willReturn(oauthSession);
        given(googleSocialCommandService.registerOrLoginBySocialCode(any(GoogleOauthLoginCommand.class)))
                .willReturn(memberId);

        // Act
        googleSocialRedirectCommandService.loginByRedirect(command);

        // Assert
        verify(googleOauthSessionResolver).resolveAndConsume(command);
        verify(googleSocialCommandService).registerOrLoginBySocialCode(any(GoogleOauthLoginCommand.class));
        verify(socialLoginTokenPort).authenticateBySession(memberId);
    }
}
