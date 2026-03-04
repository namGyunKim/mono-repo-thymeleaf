package com.example.global.config.swagger;

import com.example.global.security.SecurityPublicPaths;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * Swagger UI에서 Authorize 토큰을 실제 요청에 반영하기 위해,
 * API 경로에 Bearer 인증 요구사항을 기본으로 등록하는 커스터마이저
 */
@Component
class BearerAuthSwaggerCustomizer implements OpenApiCustomizer {

    private static final String BEARER_SECURITY_SCHEME = "Bearer Authentication";

    @Override
    public void customise(final io.swagger.v3.oas.models.OpenAPI openApi) {
        if (openApi == null || openApi.getPaths() == null) {
            return;
        }
        openApi.getPaths().forEach(this::addBearerSecurityIfRequired);
    }

    private void addBearerSecurityIfRequired(final String path, final PathItem pathItem) {
        if (path == null || pathItem == null) {
            return;
        }
        if (!path.startsWith("/api/")) {
            return;
        }
        if (SecurityPublicPaths.isPublicApiPath(path)) {
            return;
        }
        pathItem.readOperations().forEach(this::addBearerSecurityItem);
    }

    private void addBearerSecurityItem(final Operation operation) {
        if (operation == null) {
            return;
        }

        final boolean alreadyAdded = operation.getSecurity() != null
                && operation.getSecurity().stream().anyMatch(requirement -> requirement != null
                && requirement.containsKey(BEARER_SECURITY_SCHEME));

        if (alreadyAdded) {
            return;
        }

        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_SECURITY_SCHEME));
    }
}
