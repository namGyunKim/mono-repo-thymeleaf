package com.example.domain.account.api;

import com.example.domain.account.payload.dto.AccountLogoutCommand;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.account.service.command.AccountCommandService;
import com.example.global.annotation.CurrentAccount;
import com.example.global.api.RestApiController;
import com.example.global.version.ApiVersioning;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "app.type", havingValue = "user")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class AccountSessionApiController {

    private final AccountCommandService accountCommandService;
    private final RestApiController restApiController;

    @DeleteMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated()")
    public ResponseEntity<Void> logout(@CurrentAccount final CurrentAccountDTO currentAccount) {
        accountCommandService.logout(AccountLogoutCommand.of(currentAccount));
        return restApiController.noContent();
    }
}
