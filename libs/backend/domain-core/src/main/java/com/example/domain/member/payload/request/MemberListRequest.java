package com.example.domain.member.payload.request;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberActiveStatus;
import com.example.domain.contract.enums.ApiMemberFilterType;
import com.example.domain.contract.enums.ApiMemberOrderType;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberFilterType;
import com.example.domain.member.enums.MemberOrderType;
import com.example.global.utils.PaginationUtils;


import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record MemberListRequest(
        @NotNull(message = "권한은 필수입니다.")
        ApiAccountRole role,

        Integer page,

        Integer size,

        ApiMemberOrderType order,

        String searchWord,

        ApiMemberFilterType filter,

        ApiMemberActiveStatus active
) {

    // 생성자에서 null 또는 유효하지 않은 값에 대한 기본값 설정
    public MemberListRequest {
        page = PaginationUtils.normalizePage(page);
        size = PaginationUtils.normalizeSize(size, PaginationUtils.DEFAULT_SIZE);
        order = Objects.requireNonNullElse(order, ApiMemberOrderType.CREATE_DESC);
        searchWord = Objects.requireNonNullElse(searchWord, "");
        filter = Objects.requireNonNullElse(filter, ApiMemberFilterType.ALL);
        active = Objects.requireNonNullElse(active, ApiMemberActiveStatus.ALL);
    }

    public static MemberListRequest of(
            final ApiAccountRole role,
            final Integer page,
            final Integer size,
            final ApiMemberOrderType order,
            final String searchWord,
            final ApiMemberFilterType filter,
            final ApiMemberActiveStatus active
    ) {
        return new MemberListRequest(role, page, size, order, searchWord, filter, active);
    }

    public static MemberListRequest defaultRequest() {
        return of(ApiAccountRole.USER, PaginationUtils.DEFAULT_PAGE, PaginationUtils.DEFAULT_SIZE, null, "", null, null);
    }

    public AccountRole toDomainRole() {
        return role != null ? role.toDomain() : null;
    }

    public MemberOrderType toDomainOrder() {
        return order != null ? order.toDomain() : null;
    }

    public MemberFilterType toDomainFilter() {
        return filter != null ? filter.toDomain() : null;
    }

    public MemberActiveStatus toDomainActive() {
        return active != null ? active.toDomain() : null;
    }
}
