package com.example.domain.member.support;

/**
 * Member 도메인이 회원 탈퇴 시 소셜 연동 정리를 요청하는 Port
 *
 * <p>
 * - Member 도메인은 SocialAccountRepository / GoogleSocialCommandService를 직접 참조하지 않습니다.
 * - Port 정의: member/support (사용하는 도메인)
 * - Adapter 구현: social/support (제공하는 도메인)
 * </p>
 */
public interface MemberSocialCleanupPort {

    /**
     * 회원 탈퇴 시 소셜 연동을 정리합니다.
     *
     * <p>
     * - 소셜 제공자(Google 등) 토큰 revoke
     * - 소셜 계정 레코드 삭제
     * - 소셜 계정이 없으면 무시합니다.
     * </p>
     *
     * @param memberId null이 아닌 탈퇴 대상 회원 ID
     * @param loginId  null이 아닌 비어 있지 않은 탈퇴 대상 회원의 로그인 ID
     */
    void cleanupOnWithdraw(final Long memberId, final String loginId);
}
