package com.example.global.config.web.support;

import com.example.global.utils.LoggingSanitizerPolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.StringJoiner;

@Component
public class FallbackRequestUriBuilder {

    public String buildUriWithQuery(final HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        final String uri = safe(request.getRequestURI());
        final String query = buildSanitizedQueryString(request);
        if (!hasText(query)) {
            return uri;
        }

        return uri + "?" + query;
    }

    private String buildSanitizedQueryString(final HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        final Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames == null || !paramNames.hasMoreElements()) {
            return "";
        }

        final StringJoiner joiner = new StringJoiner("&");
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            if (!hasText(name)) {
                continue;
            }

            final String maskedName = maskKeyIfSensitive(name);
            final String[] values = request.getParameterValues(name);
            if (values == null || values.length == 0) {
                joiner.add(maskedName + "=" + safe(maskIfSensitive(name, "")));
                continue;
            }

            for (final String value : values) {
                joiner.add(maskedName + "=" + safe(maskIfSensitive(name, value)));
            }
        }
        return joiner.toString();
    }

    private String maskIfSensitive(final String name, final String value) {
        if (LoggingSanitizerPolicy.isSensitiveField(name)) {
            return "***";
        }
        return value;
    }

    private String maskKeyIfSensitive(final String name) {
        if (LoggingSanitizerPolicy.isSensitiveField(name)) {
            return "***";
        }
        return safe(name);
    }

    private String safe(final String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(final String value) {
        return value != null && !value.trim().isEmpty();
    }
}
