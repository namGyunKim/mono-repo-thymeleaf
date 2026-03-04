package com.example.global.security.filter.support;

import com.example.domain.account.validator.LoginRequestRoleStrategy;
import com.example.global.payload.response.ApiErrorDetail;

import java.util.List;

public record LoginRequestValidationResult(
        Object loginRequest,
        LoginRequestRoleStrategy strategy,
        String loginId,
        String password,
        List<ApiErrorDetail> errors
) {
    public LoginRequestValidationResult {
        errors = (errors == null) ? List.of() : List.copyOf(errors);
    }

    public static LoginRequestValidationResult of(
            final Object loginRequest,
            final LoginRequestRoleStrategy strategy,
            final String loginId,
            final String password,
            final List<ApiErrorDetail> errors
    ) {
        return new LoginRequestValidationResult(loginRequest, strategy, loginId, password, errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format(
                "LoginRequestValidationResult[loginId=%s, strategy=%s, hasErrors=%s, password=***]",
                loginId, strategy, hasErrors()
        );
    }
}
