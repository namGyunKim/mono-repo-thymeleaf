package com.example.global.security.service.query;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.member.enums.MemberType;
import com.example.domain.security.guard.PrincipalDetails;
import com.example.domain.security.port.SecurityAccountAuthQueryPort;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.GlobalException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체로, 로그인 아이디 기반으로 인증 정보를 로드한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrincipalDetailsQueryService implements UserDetailsService {

    private final SecurityAccountAuthQueryPort securityAccountAuthQueryPort;

    // 로그인 아이디로 유저 정보 로드 (Spring Security Form Login의 핵심)
    // Spring Security의 기본 Form Login은 'username' 파라미터만 사용하여 이 메서드를 호출합니다.
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        try {
            // [수정] Form Login 시 `role` 파라미터를 함께 전달받지 못하므로,
            // SecurityAccountAuthQueryPort는 `loginId`만으로 찾도록 합니다.
            // *단, 내부 구현은 Active 체크를 수행합니다.*
            final AccountAuthMemberView member = securityAccountAuthQueryPort.findActiveMemberForAuthByLoginId(AccountLoginIdQuery.of(username));
            if (member.memberType() != MemberType.GENERAL) {
                throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
            }
            return new PrincipalDetails(member);
        } catch (final GlobalException e) {
            throw translateGlobalException(e, username);
        }
    }

    /**
     * GlobalException을 Spring Security 표준 AuthenticationException으로 변환합니다.
     * - MEMBER_INACTIVE → DisabledException (비활성 계정)
     * - MEMBER_NOT_EXIST → UsernameNotFoundException (미존재 계정)
     * - 그 외 → InternalAuthenticationServiceException (내부 인증 오류)
     */
    private RuntimeException translateGlobalException(final GlobalException e, final String username) {
        if (e.getErrorCode() == ErrorCode.MEMBER_INACTIVE) {
            return new DisabledException("비활성화된 계정입니다: " + username, e);
        }

        if (e.getErrorCode() == ErrorCode.MEMBER_NOT_EXIST) {
            return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username, e);
        }

        // 그 외 예외는 내부 인증 처리 실패로 전환 (Spring Security 표준 예외)
        return new InternalAuthenticationServiceException("인증 처리 중 오류가 발생했습니다.", e);
    }
}
