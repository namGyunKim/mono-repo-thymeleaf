package com.example.domain.account.support;

/**
 * account 도메인에서 security 도메인의 토큰 폐기 기능을 사용하는 포트.
 *
 * <p>방향: account → security
 *
 * <p>AccountCommandService가 JwtTokenRevocationCommandService에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface AccountTokenRevocationPort {

    /**
     * 로그아웃 시 해당 회원의 토큰을 폐기한다.
     *
     * <p>액세스 토큰을 블랙리스트에 등록하고, 리프레시 토큰을 무효화한다.
     *
     * @param memberId    null이 아닌 로그아웃 대상 회원 ID
     * @param accessToken null이 아닌 비어 있지 않은 현재 액세스 토큰
     */
    void revokeOnLogout(final Long memberId, final String accessToken);
}
