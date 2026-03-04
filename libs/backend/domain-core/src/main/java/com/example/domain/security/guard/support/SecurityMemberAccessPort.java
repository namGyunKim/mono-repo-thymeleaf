package com.example.domain.security.guard.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountAuthMemberView;

import java.util.List;
import java.util.Optional;

/**
 * Security 도메인이 회원 접근/인증 관련 정보를 조회할 때 사용하는 Port
 *
 * <p>
 * - Security 도메인은 MemberRepository / Member 엔티티를 직접 참조하지 않고 이 Port를 통해 접근합니다.
 * - Port 정의: security/guard/support (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * </p>
 */
public interface SecurityMemberAccessPort {

    /**
     * 회원 ID로 접근 대상 정보를 조회한다.
     *
     * @param memberId null이 아닌 조회할 회원 ID
     * @return 해당 회원의 접근 대상 정보, 존재하지 않으면 empty
     */
    Optional<MemberAccessTarget> findAccessTargetById(final Long memberId);

    /**
     * 회원 ID와 역할 목록으로 접근 대상 정보를 조회한다.
     *
     * <p>지정된 역할 중 하나라도 일치하는 회원만 반환한다.
     *
     * @param memberId null이 아닌 조회할 회원 ID
     * @param roles    null이 아닌 비어 있지 않은 허용 역할 목록
     * @return 해당 회원의 접근 대상 정보, 조건에 맞지 않으면 empty
     */
    Optional<MemberAccessTarget> findAccessTargetByIdAndRoleIn(final Long memberId, final List<AccountRole> roles);

    /**
     * 로그인 ID로 활성 회원의 인증 정보를 조회한다.
     *
     * <p>JWT 인증 필터에서 토큰 subject 기반 회원 조회에 사용한다.
     *
     * @param loginId null이 아닌 비어 있지 않은 조회할 로그인 ID
     * @return 해당 회원의 인증 정보 뷰, 존재하지 않으면 empty
     */
    Optional<AccountAuthMemberView> findActiveAuthMemberByLoginId(final String loginId);

    /**
     * 로그인 ID로 회원 ID를 조회한다.
     *
     * <p>로그인 실패 이벤트 발행 시 회원 식별에 사용한다.
     *
     * @param loginId null이 아닌 비어 있지 않은 조회할 로그인 ID
     * @return 해당 회원의 ID, 존재하지 않으면 empty
     */
    Optional<Long> findMemberIdByLoginId(final String loginId);
}
