package com.example.global.logging;

import jakarta.servlet.http.HttpServletRequest;

public record RequestMeta(
        String method,
        String path
) {
    public static RequestMeta from(final HttpServletRequest request) {
        final String method = request != null ? request.getMethod() : "";
        final String path = request != null ? request.getRequestURI() : "";
        return new RequestMeta(method, path);
    }
}
