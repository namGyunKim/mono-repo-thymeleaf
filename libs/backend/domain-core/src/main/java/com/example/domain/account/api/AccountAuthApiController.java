package com.example.domain.account.api;

import com.example.domain.account.payload.response.RefreshTokenResponse;
import com.example.domain.account.support.AccountTokenRefreshPort;
import com.example.global.api.RestApiController;
import com.example.global.security.SecurityHeaders;
import com.example.global.security.jwt.AccessTokenResolver;
import com.example.global.security.support.LocalTokenHeaderLoggingSupport;
import com.example.global.security.TokenResponseHeaders;
import com.example.global.version.ApiVersioning;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT 토큰 갱신 API 컨트롤러
 *
 * <p>
 * {@code @ConditionalOnProperty}를 적용하지 않는 이유:
 * 토큰 갱신은 {@code app.type}(admin/user)에 관계없이
 * 모든 애플리케이션에서 공통으로 사용되는 인증 기반 API이므로,
 * 앱 타입 조건 없이 항상 활성화합니다.
 * </p>
 */
@PreAuthorize("permitAll()")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tokens")
@Validated
public class AccountAuthApiController {

    private final AccountTokenRefreshPort accountTokenRefreshPort;
    private final RestApiController restApiController;
    private final LocalTokenHeaderLoggingSupport localTokenHeaderLoggingSupport;
    private final AccessTokenResolver accessTokenResolver;

    @PostMapping(
            version = ApiVersioning.V1,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> refresh(
            @RequestHeader(SecurityHeaders.REFRESH_TOKEN)
            @NotBlank(message = "refreshToken은 필수입니다.")
            String refreshToken,
            HttpServletRequest request) {
        final String oldAccessToken = accessTokenResolver.resolveAccessToken(request).orElse(null);
        final RefreshTokenResponse response = accountTokenRefreshPort.refreshTokens(refreshToken, oldAccessToken);
        localTokenHeaderLoggingSupport.logResponseTokenHeaders(
                "refresh",
                response.accessToken(),
                response.refreshToken()
        );
        final HttpHeaders headers = TokenResponseHeaders.of(response.accessToken(), response.refreshToken());
        return restApiController.noContentWithHeaders(headers);
    }
}
