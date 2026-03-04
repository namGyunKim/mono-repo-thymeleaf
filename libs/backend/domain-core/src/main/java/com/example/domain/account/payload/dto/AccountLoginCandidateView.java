package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;

/**
 * 로그인 요청 정책 검증 전용 조회 DTO
 */
public record AccountLoginCandidateView(
        Long id,
        String loginId,
        AccountRole role,
        MemberType memberType,
        MemberActiveStatus active
) {
    public static AccountLoginCandidateView of(final Long id, final String loginId, final AccountRole role, final MemberType memberType, final MemberActiveStatus active) {
        return new AccountLoginCandidateView(id, loginId, role, memberType, active);
    }
}
