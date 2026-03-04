package com.example.global.aop.support;

/**
 * 컨트롤러 요청/응답 로그 메시지를 생성하는 팩토리
 * <p>
 * AOP 기반 로깅에서 일관된 형식의 요청(REQ)/응답(RES) 로그 문자열을 만들 때 사용합니다.
 */
public final class ControllerLogMessageFactory {

    private ControllerLogMessageFactory() {
    }

    /**
     * 요청(REQ) 로그 메시지를 생성합니다.
     *
     * @param ip      클라이언트 IP 주소
     * @param loginId 로그인 사용자 식별자
     * @param method  HTTP 메서드 (GET, POST 등)
     * @param uri     요청 URI
     * @param params  요청 파라미터 문자열
     * @return 포맷된 요청 로그 문자열
     */
    public static String buildRequestLog(
            final String ip,
            final String loginId,
            final String method,
            final String uri,
            final String params
    ) {
        return """
                [REQ]
                IP      : %s
                User    : %s
                Method  : %s
                URI     : %s
                Params  :
                %s
                """.formatted(ip, loginId, method, uri, params).stripTrailing();
    }

    /**
     * 응답(RES) 로그 메시지를 생성합니다.
     *
     * @param elapsedMs 요청 처리에 소요된 시간 (밀리초)
     * @param status    HTTP 응답 상태 코드 문자열
     * @param size      응답 본문 크기 문자열
     * @return 포맷된 응답 로그 문자열
     */
    public static String buildResponseLog(final long elapsedMs, final String status, final String size) {
        return """
                [RES]
                Time    : %dms
                Status  : %s
                Size    : %s
                """.formatted(elapsedMs, status, size).stripTrailing();
    }
}
