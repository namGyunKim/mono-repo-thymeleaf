package com.example.global.utils;

/**
 * 문자열 공통 검사 유틸
 * <p>
 * 여러 유틸 클래스에서 반복되는 null/빈 문자열 검사를 한 곳에서 제공합니다.
 */
public final class StringCheckUtils {

    private StringCheckUtils() {
    }

    /**
     * 문자열이 null이 아니고 공백만으로 이루어지지 않았는지 검사합니다.
     *
     * @param s 검사할 문자열 (null 허용)
     * @return 문자열이 null이 아니고 공백 이외의 문자를 포함하면 {@code true}
     */
    public static boolean hasText(final String s) {
        return s != null && !s.isBlank();
    }
}
