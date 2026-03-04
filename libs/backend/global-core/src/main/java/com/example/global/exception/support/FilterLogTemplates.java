package com.example.global.exception.support;

/**
 * 필터 및 인증 관련 로그 템플릿 상수
 * - 요청 필터 로깅, 인증 진입점, 로그인 실패 등에서 사용합니다.
 */
public final class FilterLogTemplates {

    public static final String FILTER_LOG_TEMPLATE = """
            [FILTER]
            ip={}
            loginId={}
            method={}
            uri={}
            status={}
            time={}ms
            """;

    public static final String FILTER_EXCEPTION_LOG_TEMPLATE = """
            [FILTER]
            ip={}
            loginId={}
            method={}
            uri={}
            status={}
            time={}ms
            errorName={}
            errorCode={}
            errorMessage={}
            exception={}
            message={}
            """;

    public static final String AUTHENTICATION_ENTRYPOINT_LOG_TEMPLATE = """
            [AUTHENTICATION_REQUIRED]
            ip={}
            method={}
            uri={}
            errorName={}
            errorCode={}
            errorMessage={}
            message={}
            """;

    public static final String LOGIN_MISSING_CREDENTIAL_LOG_TEMPLATE = """
            [LOGIN_MISSING_CREDENTIAL]
            ip={}
            method={}
            uri={}
            loginId={}
            errors={}
            """;

    public static final String LOGIN_AUTH_FAILURE_LOG_TEMPLATE = """
            [LOGIN_AUTH_FAILURE]
            ip={}
            method={}
            uri={}
            loginId={}
            reason={}
            """;

    public static final String LOGIN_JSON_BAD_REQUEST_LOG_TEMPLATE = """
            [LOGIN_JSON_BAD_REQUEST]
            ip={}
            method={}
            uri={}
            loginId={}
            errorName={}
            errorCode={}
            errorMessage={}
            errors={}
            """;

    private FilterLogTemplates() {
    }
}
