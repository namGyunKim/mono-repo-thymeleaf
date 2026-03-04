package com.example.global.aop.support;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * HTTP 응답의 상태 코드와 본문 크기를 추출하는 유틸리티
 */
public final class ControllerResponseInfoResolver {

    private ControllerResponseInfoResolver() {
    }

    public static String getResponseStatus(final HttpServletResponse response) {
        if (response == null) {
            return "알 수 없음";
        }

        return String.valueOf(response.getStatus());
    }

    public static String getResponseSize(final HttpServletResponse response) {
        if (response == null) {
            return "알 수 없음";
        }

        final String contentLength = response.getHeader("Content-Length");
        if (contentLength != null && !contentLength.isBlank()) {
            return contentLength + "B";
        }

        if (response instanceof ContentCachingResponseWrapper wrapper) {
            return wrapper.getContentSize() + "B";
        }

        return "알 수 없음";
    }
}
