package com.example.domain.member.support;

import com.example.global.security.payload.SecurityLogoutCommand;

/**
 * member 도메인에서 security 도메인의 토큰 폐기 기능을 사용하는 포트.
 *
 * <p>방향: member → security
 *
 * <p>AdminMemberCommandService가 JwtTokenRevocationCommandService에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface MemberTokenRevocationPort {

    /**
     * 관리자에 의한 회원 강제 로그아웃 시 해당 회원의 토큰을 폐기한다.
     *
     * <p>액세스 토큰을 블랙리스트에 등록하고, 리프레시 토큰을 무효화한다.
     *
     * @param command null이 아닌 로그아웃 커맨드.
     *                memberId와 accessToken이 필수로 포함되어야 한다.
     */
    void revokeOnLogout(final SecurityLogoutCommand command);
}
