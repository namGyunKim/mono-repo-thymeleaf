package com.example.global.security.filter.support;

import com.example.global.payload.response.ApiErrorDetail;

import java.util.List;

public record LoginRequestParseResult(
        Object loginRequest,
        List<ApiErrorDetail> errors
) {
    public LoginRequestParseResult {
        errors = (errors == null) ? List.of() : List.copyOf(errors);
    }

    public static LoginRequestParseResult of(final Object loginRequest) {
        return new LoginRequestParseResult(loginRequest, List.of());
    }

    public static LoginRequestParseResult from(final List<ApiErrorDetail> errors) {
        return new LoginRequestParseResult(null, errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
