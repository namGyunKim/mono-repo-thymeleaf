package com.example.domain.account.payload.dto;

/**
 * 계정 로그아웃 Command DTO
 */
public record AccountLogoutCommand(
        CurrentAccountDTO currentAccount
) {

    public static AccountLogoutCommand of(final CurrentAccountDTO currentAccount) {
        return new AccountLogoutCommand(currentAccount);
    }
}
