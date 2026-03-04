package com.example.domain.member.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountProfileUpdateCommand;
import com.example.domain.account.support.AccountMemberCommandPort;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.service.command.MemberCommandService;
import com.example.domain.member.service.MemberStrategyFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMemberCommandPortAdapter implements AccountMemberCommandPort {

    private final MemberStrategyFactory memberStrategyFactory;

    @Override
    public Long updateMemberProfile(final AccountProfileUpdateCommand command) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandService(command.currentAccount().role());
        return commandService.updateMember(MemberUpdateCommand.of(command.nickName(), command.password(), command.currentAccount().id()));
    }

    @Override
    public void deactivateMember(final AccountRole role, final Long memberId) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandService(role);
        commandService.deactivateMember(MemberDeactivateCommand.of(memberId));
    }
}
