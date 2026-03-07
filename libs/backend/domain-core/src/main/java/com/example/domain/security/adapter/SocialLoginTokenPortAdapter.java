package com.example.domain.security.adapter;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.security.guard.PrincipalDetails;
import com.example.domain.security.port.SecurityAccountAuthQueryPort;
import com.example.domain.social.support.SocialLoginTokenPort;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialLoginTokenPortAdapter implements SocialLoginTokenPort {

    private final SecurityAccountAuthQueryPort securityAccountAuthQueryPort;

    @Override
    public void authenticateBySession(final Long memberId) {
        final AccountAuthMemberView memberView = securityAccountAuthQueryPort.findAuthMemberById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));

        final PrincipalDetails principalDetails = new PrincipalDetails(memberView);
        final UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        principalDetails,
                        null,
                        principalDetails.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
