package com.example.domain.init.support;

import com.example.domain.account.enums.AccountRole;

public interface InitMemberSeedPort {

    boolean existsByRole(final AccountRole role);

    Long seedMember(final InitMemberSeedCommand command);
}
