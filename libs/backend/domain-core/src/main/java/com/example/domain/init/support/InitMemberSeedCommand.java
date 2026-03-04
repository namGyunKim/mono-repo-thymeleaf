package com.example.domain.init.support;

import com.example.domain.account.enums.AccountRole;

public record InitMemberSeedCommand(
        String loginId,
        String nickName,
        String password,
        AccountRole role
) {

    public static InitMemberSeedCommand of(
            final String loginId,
            final String nickName,
            final String password,
            final AccountRole role
    ) {
        return new InitMemberSeedCommand(loginId, nickName, password, role);
    }
}
