package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;

import java.util.List;

/**
 * 로그인 정책 검증 조회 요청 DTO
 */
public record AccountLoginValidationQuery(
        String loginId,
        List<AccountRole> allowedRoles
) {
    public AccountLoginValidationQuery {
        allowedRoles = allowedRoles != null ? List.copyOf(allowedRoles) : List.of();
    }

    public static AccountLoginValidationQuery of(final String loginId, final List<AccountRole> allowedRoles) {
        return new AccountLoginValidationQuery(loginId, allowedRoles);
    }
}
