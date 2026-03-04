package com.example.global.security.handler;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.security.guard.MemberGuard;
import com.example.domain.security.token.JwtTokenRevocationCommandService;
import com.example.global.security.jwt.AccessTokenResolver;
import com.example.global.security.payload.SecurityLogoutCommand;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * 로그아웃 시 JWT 토큰을 폐기(블랙리스트 등록)하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenRevocationCommandService jwtTokenRevocationCommandService;
    private final MemberGuard memberGuard;
    private final AccessTokenResolver accessTokenResolver;

    @Override
    public void logout(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) {
        final String accessToken = accessTokenResolver.resolveAccessToken(request).orElse(null);
        memberGuard.getCurrentAccount()
                .map(CurrentAccountDTO::id)
                .ifPresent(memberId -> {
                    jwtTokenRevocationCommandService.revokeOnLogout(
                            SecurityLogoutCommand.of(memberId, accessToken)
                    );
                    log.info("로그아웃 완료: memberId={}", memberId);
                });
    }
}
