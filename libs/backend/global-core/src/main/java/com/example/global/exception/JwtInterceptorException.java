package com.example.global.exception;

import com.example.global.exception.enums.ErrorCode;

/** JWT 인터셉터 예외 처리 */
public class JwtInterceptorException extends BaseAppException {

    public JwtInterceptorException(final ErrorCode errorCode) {
        super(errorCode);
    }

    public JwtInterceptorException(final ErrorCode errorCode, final String errorDetailMessage) {
        super(errorCode, errorDetailMessage);
    }

    public JwtInterceptorException(final ErrorCode errorCode, final Exception exception) {
        super(errorCode, exception);
    }
}
