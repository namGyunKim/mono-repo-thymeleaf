package com.example.global.exception.support;

/**
 * 이벤트/로깅 관련 로그 템플릿 상수
 * - 예외 이벤트 리스너, 일반 로그 이벤트 등에서 사용합니다.
 */
public final class EventLogTemplates {

    public static final String EXCEPTION_EVENT_LOG_TEMPLATE = """
            [EXCEPTION_EVENT]
            txStatus={}
            exception={}
            """;

    public static final String EXCEPTION_EVENT_STRUCTURED_LOG_TEMPLATE = """
            [EXCEPTION_EVENT_STRUCTURED]
            txStatus={}
            payload={}
            """;

    public static final String EXCEPTION_EVENT_STRUCTURED_FAIL_LOG_TEMPLATE = """
            [EXCEPTION_EVENT_STRUCTURED_FAIL]
            txStatus={}
            errorCode={}
            exceptionName={}
            message={}
            """;

    public static final String LOG_EVENT_TEMPLATE = """
            [LOG_EVENT]
            txStatus={}
            message={}
            """;

    private EventLogTemplates() {
    }
}
