package com.example.global.config.swagger;

import com.example.global.version.ApiVersioning;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.PathItem;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * Swagger UI에서 API Version 헤더를 쉽게 테스트할 수 있도록, 전역 헤더 파라미터를 추가하는 커스터마이저
 *
 * <p>
 * - API 버저닝은 URL 경로(/v1 등)가 아니라, 요청 헤더(API-Version)로만 처리합니다.
 * - 예외:
 * - 헬스체크(/api/health)
 * - 소셜 로그인(/api/social/**) : 외부 OAuth Provider가 API-Version 헤더를 전달할 수 없음
 * </p>
 */
@Component
class ApiVersionSwaggerCustomizer implements OpenApiCustomizer {

    private static final String HEALTH_CHECK_PATH = "/api/health";
    private static final String SOCIAL_API_BASE_PATH = "/api/social";

    @Override
    public void customise(final io.swagger.v3.oas.models.OpenAPI openApi) {
        if (openApi == null || openApi.getPaths() == null) {
            return;
        }
        openApi.getPaths().forEach(this::addApiVersionHeaderIfRequired);
    }

    private void addApiVersionHeaderIfRequired(final String path, final PathItem pathItem) {
        if (path == null || pathItem == null) {
            return;
        }
        if (!path.startsWith("/api/")) {
            return;
        }
        if (isVersionExemptPath(path)) {
            return;
        }
        pathItem.readOperations().forEach(this::addApiVersionParameter);
    }

    private boolean isVersionExemptPath(final String path) {
        return HEALTH_CHECK_PATH.equals(path)
                || SOCIAL_API_BASE_PATH.equals(path)
                || path.startsWith(SOCIAL_API_BASE_PATH + "/");
    }

    private void addApiVersionParameter(final Operation operation) {
        if (operation == null) {
            return;
        }

        final boolean alreadyAdded = operation.getParameters() != null
                && operation.getParameters().stream().anyMatch(p -> p != null
                && "header".equalsIgnoreCase(p.getIn())
                && ApiVersioning.HEADER_NAME.equalsIgnoreCase(p.getName()));

        if (alreadyAdded) {
            return;
        }

        operation.addParametersItem(new Parameter()
                .in("header")
                .name(ApiVersioning.HEADER_NAME)
                .required(true)
                .schema(new StringSchema())
                .description("API 버전 (예: 0.0, 1.0, 2.0). 미지정 시 기본값: " + ApiVersioning.DEFAULT_VERSION + " (유효하지 않음, 프론트에서 1.0 명시 필요)"));
    }
}
