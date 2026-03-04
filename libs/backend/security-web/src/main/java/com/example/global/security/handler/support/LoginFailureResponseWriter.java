package com.example.global.security.handler.support;

import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.support.SecurityJsonResponseWriter;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * 로그인 실패 시 JSON 에러 응답을 작성하는 클래스
 */
@Component
@RequiredArgsConstructor
public class LoginFailureResponseWriter {

    private final ObjectMapper objectMapper;

    public void writeErrorResponse(
            final HttpServletResponse response,
            final HttpStatus status,
            final ErrorCode errorCode,
            final List<ApiErrorDetail> errors
    ) throws IOException {
        if (response == null || response.isCommitted()) {
            return;
        }

        final ApiErrorResponse body = ApiErrorResponse.from(errorCode, errors);
        SecurityJsonResponseWriter.writeJsonErrorResponse(response, status.value(), body, objectMapper);
    }
}
