package com.example.domain.social.google.service.command;

import com.example.domain.social.google.payload.dto.GoogleOauthLoginCommand;
import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.payload.dto.GoogleSocialRedirectCommand;
import com.example.domain.social.google.support.GoogleOauthSessionResolver;
import com.example.domain.social.support.SocialLoginTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoogleSocialRedirectCommandService {

    private final GoogleOauthSessionResolver googleOauthSessionResolver;
    private final GoogleSocialCommandService googleSocialCommandService;
    private final SocialLoginTokenPort socialLoginTokenPort;

    public Long loginByRedirect(final GoogleSocialRedirectCommand command) {
        final GoogleOauthSession oauthSession = googleOauthSessionResolver.resolveAndConsume(command);

        final Long memberId = googleSocialCommandService.registerOrLoginBySocialCode(
                GoogleOauthLoginCommand.of(command.code(), oauthSession.codeVerifier(), oauthSession.nonce())
        );
        log.info("Google 소셜 로그인 성공: memberId={}", memberId);

        socialLoginTokenPort.authenticateBySession(memberId);
        log.info("Google 소셜 로그인 세션 설정 완료: memberId={}", memberId);

        return memberId;
    }
}
