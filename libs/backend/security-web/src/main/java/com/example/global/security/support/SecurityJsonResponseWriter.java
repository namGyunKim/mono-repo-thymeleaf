package com.example.global.security.support;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Security 계층에서 JSON 에러 응답을 작성하는 유틸리티 클래스
 */
public final class SecurityJsonResponseWriter {

    private SecurityJsonResponseWriter() {
    }

    /**
     * HTTP 응답에 JSON 형식으로 에러 본문을 작성한다.
     *
     * @param response     HTTP 응답 객체
     * @param status       HTTP 상태 코드
     * @param body         응답 본문 객체 (ObjectMapper로 직렬화)
     * @param objectMapper JSON 직렬화에 사용할 ObjectMapper
     */
    public static void writeJsonErrorResponse(
            final HttpServletResponse response,
            final int status,
            final Object body,
            final ObjectMapper objectMapper
    ) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
