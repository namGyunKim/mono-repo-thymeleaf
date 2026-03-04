package com.example.domain.account.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;


import jakarta.validation.constraints.NotBlank;

public record AccountProfileUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickName,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {

    public static AccountProfileUpdateRequest of(final String nickName, final String password) {
        return new AccountProfileUpdateRequest(nickName, password);
    }
}
