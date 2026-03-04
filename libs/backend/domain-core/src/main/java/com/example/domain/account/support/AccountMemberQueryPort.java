package com.example.domain.account.support;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginCandidateView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.account.payload.dto.AccountLoginIdRoleQuery;
import com.example.domain.account.payload.dto.AccountLoginValidationQuery;
import com.example.domain.account.payload.dto.LoginMemberView;

import java.util.Optional;

/**
 * account 도메인에서 member 도메인의 회원 정보를 조회하는 포트.
 *
 * <p>방향: account → member
 *
 * <p>AccountQueryService가 MemberRepository에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface AccountMemberQueryPort {

    /**
     * 로그인 ID와 역할(role)로 로그인 회원 뷰를 조회한다.
     *
     * @param query null이 아닌 로그인 ID·역할 조합 조회 조건.
     *              query가 null이거나 내부 필드가 비어 있으면 empty를 반환한다.
     * @return 해당 회원의 로그인 뷰, 존재하지 않으면 empty
     */
    Optional<LoginMemberView> findLoginMemberView(final AccountLoginIdRoleQuery query);

    /**
     * 로그인 ID로 인증용 회원 정보를 조회한다.
     *
     * @param query null이 아닌 로그인 ID 조회 조건.
     *              query가 null이거나 loginId가 비어 있으면 empty를 반환한다.
     * @return 해당 회원의 인증 정보 뷰, 존재하지 않으면 empty
     */
    Optional<AccountAuthMemberView> findAuthMember(final AccountLoginIdQuery query);

    /**
     * 로그인 ID와 역할(role)로 인증용 회원 정보를 조회한다.
     *
     * @param query null이 아닌 로그인 ID·역할 조합 조회 조건.
     *              query가 null이거나 내부 필드가 비어 있으면 empty를 반환한다.
     * @return 해당 회원의 인증 정보 뷰, 존재하지 않으면 empty
     */
    Optional<AccountAuthMemberView> findAuthMember(final AccountLoginIdRoleQuery query);

    /**
     * 로그인 정책 검증을 위한 후보 회원 정보를 조회한다.
     *
     * @param query null이 아닌 로그인 ID·허용 역할 목록 조회 조건.
     *              query가 null이거나 loginId 또는 allowedRoles가 비어 있으면 empty를 반환한다.
     * @return 로그인 후보 회원 뷰, 존재하지 않으면 empty
     */
    Optional<AccountLoginCandidateView> findLoginCandidate(final AccountLoginValidationQuery query);

    /**
     * 회원 ID로 인증용 회원 정보를 조회한다.
     *
     * @param memberId null이 아닌 조회할 회원 ID
     * @return 해당 회원의 인증 정보 뷰, 존재하지 않으면 empty
     */
    Optional<AccountAuthMemberView> findAuthMemberById(final Long memberId);
}
