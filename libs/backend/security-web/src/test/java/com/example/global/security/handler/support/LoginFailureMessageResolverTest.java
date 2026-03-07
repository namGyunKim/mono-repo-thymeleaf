package com.example.global.security.handler.support;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class LoginFailureMessageResolverTest {

    private final LoginFailureMessageResolver resolver = new LoginFailureMessageResolver();

    @Test
    void resolve_null_returns_default() {
        assertThat(resolver.resolve(null)).isEqualTo("로그인 실패");
    }

    @Test
    void resolve_bad_credentials_exception_returns_password_mismatch() {
        assertThat(resolver.resolve(new BadCredentialsException("bad"))).isEqualTo("비밀번호 불일치");
    }

    @Test
    void resolve_username_not_found_exception_returns_account_not_found() {
        assertThat(resolver.resolve(new UsernameNotFoundException("not found"))).isEqualTo("계정 없음");
    }

    @Test
    void resolve_internal_authentication_service_exception_returns_system_error() {
        assertThat(resolver.resolve(new InternalAuthenticationServiceException("internal"))).isEqualTo("내부 시스템 에러");
    }

    @Test
    void resolve_locked_exception_returns_account_locked() {
        assertThat(resolver.resolve(new LockedException("locked"))).isEqualTo("계정 잠김");
    }

    @Test
    void resolve_disabled_exception_returns_account_disabled() {
        assertThat(resolver.resolve(new DisabledException("disabled"))).isEqualTo("계정 비활성화");
    }

    @Test
    void resolve_account_expired_exception_returns_account_expired() {
        assertThat(resolver.resolve(new AccountExpiredException("expired"))).isEqualTo("계정 만료");
    }

    @Test
    void resolve_credentials_expired_exception_returns_password_expired() {
        assertThat(resolver.resolve(new CredentialsExpiredException("cred expired"))).isEqualTo("비밀번호 만료");
    }
}
