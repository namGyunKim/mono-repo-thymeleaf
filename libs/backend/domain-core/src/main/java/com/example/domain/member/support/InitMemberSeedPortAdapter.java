package com.example.domain.member.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.init.support.InitMemberSeedCommand;
import com.example.domain.init.support.InitMemberSeedPort;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberRoleExistsQuery;
import com.example.domain.member.service.MemberStrategyFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitMemberSeedPortAdapter implements InitMemberSeedPort {

    private final MemberStrategyFactory memberStrategyFactory;

    @Override
    public boolean existsByRole(final AccountRole role) {
        return memberStrategyFactory.getQueryService(role)
                .existsByRole(MemberRoleExistsQuery.of(role));
    }

    @Override
    public Long seedMember(final InitMemberSeedCommand command) {
        final MemberCreateCommand createCommand = MemberCreateCommand.of(
                command.loginId(),
                command.nickName(),
                command.password(),
                command.role(),
                MemberType.GENERAL
        );
        return memberStrategyFactory.getCommandService(command.role()).createMember(createCommand);
    }
}
