package com.example.global.security.handler.support;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class LoginSuccessMessageResolverTest {

    private final LoginSuccessMessageResolver resolver = new LoginSuccessMessageResolver();

    @Test
    void resolve_null_request_returns_default() {
        assertThat(resolver.resolve(null)).isEqualTo("로그인 성공");
    }

    @Test
    void resolve_any_request_returns_default() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/sessions");
        assertThat(resolver.resolve(request)).isEqualTo("로그인 성공");
    }
}
