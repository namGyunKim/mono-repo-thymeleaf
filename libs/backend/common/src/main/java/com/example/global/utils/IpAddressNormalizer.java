package com.example.global.utils;

/**
 * IP 주소 정규화 유틸
 * - IPv4:port, IPv6 bracket, 인용부호 등을 처리하여 순수 IP 주소만 반환합니다.
 * - {@link ClientIpExtractor}에서 헤더 파싱 후 정규화할 때 사용합니다.
 */
final class IpAddressNormalizer {

    private IpAddressNormalizer() {
    }

    /**
     * 원시 IP 문자열을 정규화합니다.
     * - 인용부호 제거
     * - "unknown" 값 필터링
     * - IPv6 bracket 표기에서 순수 주소 추출
     * - IPv4:port 에서 포트 제거
     */
    static String normalizeIp(final String raw) {
        if (!StringCheckUtils.hasText(raw)) {
            return null;
        }

        final String v = stripQuotes(raw.trim());
        if (!StringCheckUtils.hasText(v) || "unknown".equalsIgnoreCase(v)) {
            return null;
        }

        // IPv6 bracket + port 형태: [2001:db8::1]:1234
        if (v.startsWith("[")) {
            final int end = v.indexOf(']');
            if (end > 0) {
                return v.substring(1, end);
            }
            return v.substring(1);
        }

        // IPv4:port 형태: 1.2.3.4:1234
        if (looksLikeIpv4WithPort(v)) {
            return v.substring(0, v.indexOf(':'));
        }

        // 그 외는 그대로 반환(IPv6 등)
        return v;
    }

    /**
     * 문자열이 IPv4:port 형식인지 판별합니다.
     * - ':'가 1개이고, '.'를 포함하면 IPv4:port로 간주합니다.
     */
    static boolean looksLikeIpv4WithPort(final String v) {
        if (!StringCheckUtils.hasText(v)) {
            return false;
        }

        final int firstColon = v.indexOf(':');
        if (firstColon < 0) {
            return false;
        }
        if (v.indexOf(':', firstColon + 1) >= 0) {
            return false;
        }
        return v.contains(".");
    }

    /**
     * 구분자를 기준으로 첫 번째 토큰을 반환합니다.
     */
    static String firstToken(final String raw, final String delimiter) {
        if (!StringCheckUtils.hasText(raw)) {
            return null;
        }

        final String[] tokens = raw.split(delimiter);
        if (tokens.length == 0) {
            return null;
        }

        final String first = tokens[0].trim();
        return StringCheckUtils.hasText(first) ? first : null;
    }

    /**
     * 문자열 양 끝의 인용부호(쌍따옴표, 홑따옴표)를 제거합니다.
     */
    static String stripQuotes(final String v) {
        if (!StringCheckUtils.hasText(v)) {
            return v;
        }

        final String s = v.trim();
        if (s.length() >= 2) {
            final char first = s.charAt(0);
            final char last = s.charAt(s.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return s.substring(1, s.length() - 1).trim();
            }
        }
        return s;
    }

}
