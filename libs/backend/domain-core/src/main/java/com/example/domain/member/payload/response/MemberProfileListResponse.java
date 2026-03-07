package com.example.domain.member.payload.response;

import com.example.domain.member.entity.Member;

/**
 * 회원 목록/요약 프로필 응답 — 식별 정보 + 권한/유형 정보로 구성
 */
public record MemberProfileListResponse(
        MemberIdentityResponse identity,
        MemberAuthorizationResponse authorization
) {

    public static MemberProfileListResponse from(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberProfileListResponse(
                MemberIdentityResponse.from(member),
                MemberAuthorizationResponse.from(member)
        );
    }
}
