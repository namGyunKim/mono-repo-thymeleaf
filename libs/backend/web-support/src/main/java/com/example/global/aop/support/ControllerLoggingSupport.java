package com.example.global.aop.support;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerLoggingSupport {

    private final ControllerParamsFormatter paramsFormatter;

    public String formatParams(final ProceedingJoinPoint joinPoint) {
        return paramsFormatter.formatParams(joinPoint);
    }

    public String buildRequestLog(
            final String ip,
            final String loginId,
            final String method,
            final String uri,
            final String params
    ) {
        return ControllerLogMessageFactory.buildRequestLog(ip, loginId, method, uri, params);
    }

    public String buildResponseLog(final long elapsedMs, final String status, final String size) {
        return ControllerLogMessageFactory.buildResponseLog(elapsedMs, status, size);
    }

    public String getResponseStatus(final HttpServletResponse response) {
        return ControllerResponseInfoResolver.getResponseStatus(response);
    }

    public String getResponseSize(final HttpServletResponse response) {
        return ControllerResponseInfoResolver.getResponseSize(response);
    }
}
