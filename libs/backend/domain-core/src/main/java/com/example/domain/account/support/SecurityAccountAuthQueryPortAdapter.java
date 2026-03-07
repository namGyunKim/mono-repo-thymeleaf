package com.example.domain.account.support;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.account.service.query.AccountQueryService;
import com.example.domain.security.port.SecurityAccountAuthQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityAccountAuthQueryPortAdapter implements SecurityAccountAuthQueryPort {

    private final AccountQueryService accountQueryService;
    private final AccountMemberQueryPort accountMemberQueryPort;

    @Override
    public AccountAuthMemberView findActiveMemberForAuthByLoginId(final AccountLoginIdQuery query) {
        return accountQueryService.findActiveMemberForAuthByLoginId(query);
    }

    @Override
    public Optional<AccountAuthMemberView> findAuthMemberById(final Long memberId) {
        return accountMemberQueryPort.findAuthMemberById(memberId);
    }
}
