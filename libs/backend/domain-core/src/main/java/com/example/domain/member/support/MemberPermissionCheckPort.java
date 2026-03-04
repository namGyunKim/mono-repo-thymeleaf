package com.example.domain.member.support;

/**
 * member 도메인에서 security 도메인의 권한 확인 기능을 사용하는 포트.
 *
 * <p>방향: member → security
 *
 * <p>MemberCommandService가 MemberGuard에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface MemberPermissionCheckPort {

    /**
     * 현재 인증된 사용자가 대상 회원과 동일한지 확인한다.
     *
     * <p>관리자가 본인 계정을 삭제하는 것을 방지하는 등의 권한 검증에 사용한다.
     *
     * @param targetMemberId null이 아닌 비교 대상 회원 ID
     * @return 현재 인증된 사용자와 대상 회원이 동일하면 {@code true}
     */
    boolean isSameMember(final Long targetMemberId);
}
