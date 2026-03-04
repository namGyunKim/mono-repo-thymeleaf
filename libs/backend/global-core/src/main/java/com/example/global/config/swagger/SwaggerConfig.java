package com.example.global.config.swagger;

import com.example.global.security.SecurityHeaders;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 전역 설정
 * - OpenAPI 그룹, 보안 스키마 정의를 담당합니다.
 * - API Version 헤더 커스터마이징은 {@link ApiVersionSwaggerCustomizer}에 위임합니다.
 * - Bearer 인증 커스터마이징은 {@link BearerAuthSwaggerCustomizer}에 위임합니다.
 */
@OpenAPIDefinition(info = @Info(title = "SAMPLE API"))
@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER,
        paramName = SecurityHeaders.AUTHORIZATION)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi openApi() {
        return GroupedOpenApi.builder()
                .group("api")
                // REST API 문서 기준: /api/**
                .pathsToMatch("/api/**")
                .build();
    }
}
