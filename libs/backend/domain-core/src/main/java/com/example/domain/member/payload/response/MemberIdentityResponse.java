package com.example.domain.member.payload.response;

import com.example.domain.member.entity.Member;

/**
 * 회원 식별 정보 (id, loginId, nickName)
 */
public record MemberIdentityResponse(
        Long id,
        String loginId,
        String nickName
) {

    public static MemberIdentityResponse from(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberIdentityResponse(
                member.getId(),
                member.getLoginId(),
                member.getNickName()
        );
    }

    public static MemberIdentityResponse of(final Long id, final String loginId, final String nickName) {
        return new MemberIdentityResponse(id, loginId, nickName);
    }
}
