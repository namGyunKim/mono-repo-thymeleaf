package com.example.domain.member.payload.response;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberType;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberType;

/**
 * 회원 권한/유형 정보 (role, memberType)
 */
public record MemberAuthorizationResponse(
        ApiAccountRole role,
        ApiMemberType memberType
) {

    public static MemberAuthorizationResponse from(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberAuthorizationResponse(
                ApiAccountRole.fromDomain(member.getRole()),
                ApiMemberType.fromDomain(member.getMemberType())
        );
    }

    public static MemberAuthorizationResponse of(final AccountRole role, final MemberType memberType) {
        return new MemberAuthorizationResponse(
                ApiAccountRole.fromDomain(role),
                ApiMemberType.fromDomain(memberType)
        );
    }
}
