package com.example.global.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 클라이언트 IP 추출 유틸
 * - 프록시/LB(예: AWS ALB), CDN(예: Cloudflare) 환경을 고려합니다.
 * - 인프라에서 헤더 스푸핑 방지를 위한 "신뢰 프록시" 설정이 별도로 필요할 수 있으나,
 * 베이스 프로젝트 특성상 여기서는 다양한 헤더를 순서대로 확인하는 방식으로 구현합니다.
 * - IP 정규화(포트 제거, 인용부호 제거 등)는 {@link IpAddressNormalizer}에 위임합니다.
 */
public final class ClientIpExtractor {

    private static final List<String> IP_HEADERS = List.of(
            "CF-Connecting-IP",     // Cloudflare
            "X-Real-IP",            // Nginx 등
            "X-Forwarded-For",      // 프록시 체인
            "Forwarded"             // RFC 7239
    );

    private ClientIpExtractor() {
    }

    /**
     * 가능한 경우 실제 클라이언트 IP를 반환합니다.
     * - 값이 없거나 파싱 실패 시 request.getRemoteAddr()로 폴백합니다.
     */
    public static String extract(final HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }

        try {
            final String fromHeaders = resolveFromHeaders(request);
            if (fromHeaders != null) {
                return fromHeaders;
            }

            final String remoteAddr = request.getRemoteAddr();
            return StringCheckUtils.hasText(remoteAddr) ? remoteAddr.trim() : "UNKNOWN";
        } catch (final Exception e) {
            return "UNKNOWN";
        }
    }

    private static String resolveFromHeaders(final HttpServletRequest request) {
        for (final String header : IP_HEADERS) {
            final String raw = request.getHeader(header);
            final String candidate = parse(header, raw);
            if (StringCheckUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String parse(final String headerName, final String raw) {
        if (!StringCheckUtils.hasText(raw)) {
            return null;
        }

        final String v = raw.trim();
        if (!StringCheckUtils.hasText(v) || "unknown".equalsIgnoreCase(v)) {
            return null;
        }

        return switch (headerName) {
            case "X-Forwarded-For" -> IpAddressNormalizer.normalizeIp(IpAddressNormalizer.firstToken(v, ","));
            case "Forwarded" -> IpAddressNormalizer.normalizeIp(parseForwardedFor(v));
            default -> IpAddressNormalizer.normalizeIp(v);
        };
    }

    /**
     * Forwarded 헤더 예시:
     * - Forwarded: for=192.0.2.60;proto=http;by=203.0.113.43
     * - Forwarded: for="[2001:db8:cafe::17]:4711"
     */
    private static String parseForwardedFor(final String forwarded) {
        if (!StringCheckUtils.hasText(forwarded)) {
            return null;
        }

        final String first = IpAddressNormalizer.firstToken(forwarded, ",");
        if (!StringCheckUtils.hasText(first)) {
            return null;
        }

        for (final String part : first.split(";")) {
            final String forValue = extractForwardedForValue(part);
            if (forValue != null) {
                return forValue;
            }
        }

        return null;
    }

    private static String extractForwardedForValue(final String part) {
        final String p = part.trim();
        if (p.isEmpty() || !p.regionMatches(true, 0, "for=", 0, 4)) {
            return null;
        }
        return IpAddressNormalizer.stripQuotes(p.substring(4).trim());
    }

}
