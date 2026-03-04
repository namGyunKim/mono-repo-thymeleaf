package com.example.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class SecurityPublicPathsTest {

    @Test
    void isPublicApiPath_null_returns_false() {
        assertThat(SecurityPublicPaths.isPublicApiPath(null)).isFalse();
    }

    @Test
    void isPublicApiPath_blank_returns_false() {
        assertThat(SecurityPublicPaths.isPublicApiPath("   ")).isFalse();
    }

    @Test
    void isPublicApiPath_health_returns_true() {
        assertThat(SecurityPublicPaths.isPublicApiPath("/api/health")).isTrue();
    }

    @Test
    void isPublicApiPath_sessions_returns_true() {
        assertThat(SecurityPublicPaths.isPublicApiPath("/api/sessions")).isTrue();
    }

    @Test
    void isPublicApiPath_social_exact_returns_true() {
        assertThat(SecurityPublicPaths.isPublicApiPath("/api/social")).isTrue();
    }

    @Test
    void isPublicApiPath_social_subpath_returns_true() {
        assertThat(SecurityPublicPaths.isPublicApiPath("/api/social/google/redirect")).isTrue();
    }

    @Test
    void isPublicApiPath_members_returns_false() {
        assertThat(SecurityPublicPaths.isPublicApiPath("/api/members")).isFalse();
    }

    @Test
    void isPublicApiPath_socialx_no_slash_returns_false() {
        assertThat(SecurityPublicPaths.isPublicApiPath("/api/socialx")).isFalse();
    }
}
