package com.example.domain.account.payload.dto;

/**
 * 계정 로그아웃 Command DTO
 */
public record AccountLogoutCommand(
        CurrentAccountDTO currentAccount,
        String accessToken
) {

    public static AccountLogoutCommand of(final CurrentAccountDTO currentAccount, final String accessToken) {
        return new AccountLogoutCommand(currentAccount, accessToken);
    }
}
