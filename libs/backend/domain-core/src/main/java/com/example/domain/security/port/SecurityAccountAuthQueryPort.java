package com.example.domain.security.port;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;

/**
 * Security 도메인이 계정 인증 정보를 조회할 때 사용하는 Port
 *
 * <p>
 * - Security 도메인(및 security-web)은 AccountQueryService를 직접 참조하지 않고 이 Port를 통해 접근합니다.
 * - Port 정의: security/port (사용하는 도메인)
 * - Adapter 구현: account/support (제공하는 도메인)
 * </p>
 */
public interface SecurityAccountAuthQueryPort {

    /**
     * 로그인 ID로 활성 상태인 회원의 인증 정보를 조회한다.
     *
     * <p>Spring Security 인증 과정에서 UserDetailsService가 호출하며,
     * 비활성 회원은 조회 대상에서 제외된다.
     *
     * @param query null이 아닌 로그인 ID 조회 조건
     * @return 활성 상태인 회원의 인증 정보 뷰 (null이 아님)
     * @throws com.example.global.exception.GlobalException 해당 로그인 ID의 활성 회원이 존재하지 않을 경우
     */
    AccountAuthMemberView findActiveMemberForAuthByLoginId(final AccountLoginIdQuery query);
}
