package com.example.domain.member.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;


import jakarta.validation.constraints.NotBlank;

public record MemberUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickName,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {

    public static MemberUpdateRequest of(final String nickName, final String password) {
        return new MemberUpdateRequest(nickName, password);
    }
}
