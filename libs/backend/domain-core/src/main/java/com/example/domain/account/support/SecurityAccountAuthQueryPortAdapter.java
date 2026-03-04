package com.example.domain.account.support;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.account.service.query.AccountQueryService;
import com.example.domain.security.port.SecurityAccountAuthQueryPort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityAccountAuthQueryPortAdapter implements SecurityAccountAuthQueryPort {

    private final AccountQueryService accountQueryService;

    @Override
    public AccountAuthMemberView findActiveMemberForAuthByLoginId(final AccountLoginIdQuery query) {
        return accountQueryService.findActiveMemberForAuthByLoginId(query);
    }
}
