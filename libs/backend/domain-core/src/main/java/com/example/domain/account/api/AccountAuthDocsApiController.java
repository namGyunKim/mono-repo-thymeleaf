package com.example.domain.account.api;

import com.example.domain.account.payload.request.AccountUserLoginRequest;
import com.example.global.api.RestApiController;
import com.example.global.version.ApiVersioning;


import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 엔드포인트는 Spring Security 인증 필터에서 처리되므로,
 * OpenAPI/Swagger 문서 노출을 위한 전용 컨트롤러를 제공합니다.
 *
 * <p>
 * - 실제 인증 로직은 JsonBodyLoginAuthenticationFilter(또는 formLogin 필터)에서 수행됩니다.
 * - 본 컨트롤러는 "문서 스키마 제공" 목적이며, 정상적인 구성에서는 호출되지 않습니다.
 * </p>
 */
@PreAuthorize("permitAll()")
@ConditionalOnProperty(name = "app.type", havingValue = "user")
@RestController
@RequiredArgsConstructor
public class AccountAuthDocsApiController {

    private final RestApiController restApiController;

    @PostMapping(
            value = "/api/sessions",
            version = ApiVersioning.V1,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> loginJson(@Valid @RequestBody final AccountUserLoginRequest accountUserLoginRequest) {
        // 문서 노출용 Stub (실제 호출 시도는 Filter가 선처리합니다.)
        return restApiController.noContent();
    }
}
