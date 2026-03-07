package com.example.domain.account.payload.response;

import com.example.domain.account.payload.dto.LoginMemberView;
import com.example.domain.contract.enums.ApiMemberActiveStatus;
import com.example.domain.member.payload.response.MemberAuthorizationResponse;
import com.example.domain.member.payload.response.MemberIdentityResponse;

/**
 * 로그인/프로필 화면에서 사용하는 회원 요약 DTO — 식별 + 권한 + 활성 상태로 구성
 */
public record LoginMemberResponse(
        MemberIdentityResponse identity,
        MemberAuthorizationResponse authorization,
        ApiMemberActiveStatus active
) {

    public static LoginMemberResponse from(final LoginMemberView view) {
        if (view == null) {
            throw new IllegalArgumentException("view는 필수입니다.");
        }

        return new LoginMemberResponse(
                MemberIdentityResponse.of(view.id(), view.loginId(), view.nickName()),
                MemberAuthorizationResponse.of(view.role(), view.memberType()),
                ApiMemberActiveStatus.fromDomain(view.active())
        );
    }
}
