package com.example.domain.account.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountUserLoginRequest(
        @NotBlank(message = "로그인 아이디를 입력해주세요.")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하로 입력해주세요.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {

    public static AccountUserLoginRequest of(final String loginId, final String password) {
        return new AccountUserLoginRequest(loginId, password);
    }
}
