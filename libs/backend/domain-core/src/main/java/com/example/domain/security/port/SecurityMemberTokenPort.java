package com.example.domain.security.port;

import java.util.Optional;

/**
 * Security 도메인이 회원 토큰 관련 정보를 조회/변경할 때 사용하는 Port
 *
 * <p>
 * - Security 도메인은 MemberRepository / Member 엔티티를 직접 참조하지 않고 이 Port를 통해 접근합니다.
 * - Port 정의: security/port (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * </p>
 */
public interface SecurityMemberTokenPort {

    /**
     * 회원 ID로 토큰 발급/검증에 필요한 회원 정보를 조회한다.
     *
     * @param memberId null이 아닌 조회할 회원 ID
     * @return 해당 회원의 토큰 정보, 존재하지 않으면 empty
     */
    Optional<SecurityMemberTokenInfo> findTokenInfoById(final Long memberId);

    /**
     * 로그인 ID로 토큰 발급/검증에 필요한 회원 정보를 조회한다.
     *
     * @param loginId null이 아닌 비어 있지 않은 조회할 로그인 ID
     * @return 해당 회원의 토큰 정보, 존재하지 않으면 empty
     */
    Optional<SecurityMemberTokenInfo> findTokenInfoByLoginId(final String loginId);

    /**
     * 리프레시 토큰 암호문을 갱신한다.
     *
     * @param memberId  null이 아닌 대상 회원 ID
     * @param encrypted null이 아닌 비어 있지 않은 새 리프레시 토큰 암호문
     * @throws com.example.global.exception.GlobalException 회원이 존재하지 않을 경우
     */
    void updateRefreshTokenEncrypted(final Long memberId, final String encrypted);

    /**
     * 토큰 버전을 회전하고 리프레시 토큰을 무효화한다.
     *
     * @param memberId null이 아닌 대상 회원 ID
     * @throws com.example.global.exception.GlobalException 회원이 존재하지 않을 경우
     */
    void revokeTokens(final Long memberId);
}
