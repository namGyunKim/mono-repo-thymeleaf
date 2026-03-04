package com.example.domain.account.payload.response;

import com.example.domain.account.payload.dto.LoginMemberView;

/**
 * 로그인 성공 응답 (회원 요약 + 토큰)
 */
public record LoginTokenResponse(
        LoginMemberResponse member,
        String accessToken,
        String refreshToken
) {

    public static LoginTokenResponse from(final LoginMemberView member, final String accessToken, final String refreshToken) {
        return of(LoginMemberResponse.from(member), accessToken, refreshToken);
    }

    public static LoginTokenResponse of(final LoginMemberResponse member, final String accessToken, final String refreshToken) {
        return new LoginTokenResponse(member, accessToken, refreshToken);
    }
}
