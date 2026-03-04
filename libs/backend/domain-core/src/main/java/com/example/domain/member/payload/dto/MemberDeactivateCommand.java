package com.example.domain.member.payload.dto;

/**
 * 회원 비활성화(탈퇴) 요청 DTO
 */
public record MemberDeactivateCommand(
        Long memberId,
        Long currentAccountId
) {

    public static MemberDeactivateCommand of(final Long memberId) {
        return of(memberId, null);
    }

    public static MemberDeactivateCommand of(final Long memberId, final Long currentAccountId) {
        return new MemberDeactivateCommand(memberId, currentAccountId);
    }
}
