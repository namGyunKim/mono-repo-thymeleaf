package com.example.domain.account.api;

import com.example.domain.account.payload.dto.AccountProfileUpdateCommand;
import com.example.domain.account.payload.dto.AccountWithdrawCommand;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.account.payload.dto.LoginMemberView;
import com.example.domain.account.payload.request.AccountProfileUpdateRequest;
import com.example.domain.account.payload.response.LoginMemberResponse;
import com.example.domain.account.service.command.AccountCommandService;
import com.example.domain.account.service.query.AccountQueryService;
import com.example.global.annotation.CurrentAccount;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.IdResponse;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.version.ApiVersioning;


import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty(name = "app.type", havingValue = "user")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountQueryService accountQueryService;
    private final AccountCommandService accountCommandService;
    private final RestApiController restApiController;

    @GetMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated()")
    public ResponseEntity<RestApiResponse<LoginMemberResponse>> profile(@CurrentAccount final CurrentAccountDTO currentAccount) {
        final LoginMemberView view = accountQueryService.getLoginData(currentAccount);
        final LoginMemberResponse response = LoginMemberResponse.from(view);
        return restApiController.ok(response);
    }

    @PutMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated() and @memberGuard.canAccessSelf(#currentAccount)")
    public ResponseEntity<RestApiResponse<IdResponse>> profileUpdate(@CurrentAccount final CurrentAccountDTO currentAccount, @Valid @RequestBody final AccountProfileUpdateRequest accountProfileUpdateRequest) {
        final Long updatedId = accountCommandService.updateProfile(
                AccountProfileUpdateCommand.from(currentAccount, accountProfileUpdateRequest)
        );
        final IdResponse updateResponse = IdResponse.of(updatedId);

        return restApiController.ok(updateResponse);
    }

    @DeleteMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated() and @memberGuard.canAccessSelf(#currentAccount)")
    public ResponseEntity<Void> withdraw(@CurrentAccount final CurrentAccountDTO currentAccount) {
        accountCommandService.withdraw(AccountWithdrawCommand.of(currentAccount));
        return restApiController.noContent();
    }
}
