package com.example.domain.member.payload.request;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberType;
import com.example.domain.member.enums.MemberType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberCreateRequest(
        @NotBlank(message = "로그인 아이디를 입력해주세요.")
        String loginId,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickName,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        ApiAccountRole role,

        @NotNull(message = "멤버 타입을 선택해주세요.")
        ApiMemberType memberType
) {

    public static MemberCreateRequest of(final String loginId, final String nickName, final String password, final ApiAccountRole role, final ApiMemberType memberType) {
        return new MemberCreateRequest(loginId, nickName, password, role, memberType);
    }

    /**
     * role 파라미터 변조 방어 용도
     * - 외부에서 new 호출을 금지하기 위해, 역할 고정은 DTO 내부에서 수행한다.
     */
    public MemberCreateRequest withRole(final AccountRole role) {
        return of(loginId(), nickName(), password(), ApiAccountRole.fromDomain(role), memberType());
    }

    public AccountRole toDomainRole() {
        return role != null ? role.toDomain() : null;
    }

    public MemberType toDomainMemberType() {
        return memberType != null ? memberType.toDomain() : null;
    }
}
