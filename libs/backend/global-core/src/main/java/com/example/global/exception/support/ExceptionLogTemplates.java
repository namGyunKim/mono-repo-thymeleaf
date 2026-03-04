package com.example.global.exception.support;

/**
 * 예외 처리(ControllerAdvice) 관련 로그 템플릿 상수
 * - 필터/인증 관련 템플릿은 {@link FilterLogTemplates}를 참조합니다.
 * - 이벤트/로깅 관련 템플릿은 {@link EventLogTemplates}를 참조합니다.
 */
public final class ExceptionLogTemplates {

    public static final String TYPE_MISMATCH_LOG_TEMPLATE = """
            [EXCEPTION]
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            name={}
            value={}
            """;

    public static final String MISSING_PARAMETER_LOG_TEMPLATE = """
            [EXCEPTION]
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            name={}
            type={}
            """;

    public static final String MESSAGE_ONLY_LOG_TEMPLATE = """
            [EXCEPTION]
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            message={}
            """;

    public static final String ACCESS_DENIED_LOG_TEMPLATE = """
            [ACCESS_DENIED]
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            message={}
            """;

    public static final String UNEXPECTED_EXCEPTION_LOG_TEMPLATE = """
            [EXCEPTION]
            method={}
            path={}
            errorName={}
            errorCode={}
            errorMessage={}
            accountId={}
            loginId={}
            """;

    private ExceptionLogTemplates() {
    }
}
