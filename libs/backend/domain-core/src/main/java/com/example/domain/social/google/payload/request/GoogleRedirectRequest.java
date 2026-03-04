package com.example.domain.social.google.payload.request;


/**
 * 구글 OAuth 콜백(/api/social/v1/google/redirect)에서 사용하는 쿼리 파라미터 바인딩 전용 Request
 * <p>
 * - 컨트롤러에서 @RequestParam을 지양하기 위해 도입
 * - 구글은 성공 시 code/state를, 실패/거부 시 error/error_description 등을 전달할 수 있습니다.
 */
public record GoogleRedirectRequest(
        String code,
        String state,
        String error,
        String errorDescription
) {

    public static GoogleRedirectRequest of(final String code, final String state, final String error, final String errorDescription) {
        return new GoogleRedirectRequest(code, state, error, errorDescription);
    }
}
