package com.example.domain.account.payload.dto;

/**
 * 계정 탈퇴(비활성화) Command DTO
 */
public record AccountWithdrawCommand(
        CurrentAccountDTO currentAccount
) {

    public static AccountWithdrawCommand of(final CurrentAccountDTO currentAccount) {
        return new AccountWithdrawCommand(currentAccount);
    }
}
