package com.example.domain.log.event;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * ExceptionEvent 생성에 필요한 컨텍스트 정보를 묶는 DTO
 *
 * <p>
 * ExceptionEvent.from() 파라미터 수를 줄이기 위한 전용 record
 * </p>
 */
public record ExceptionContext(
        Exception exception,
        ErrorCode errorCode,
        String errorDetailMsg,
        CurrentAccountDTO account,
        HttpServletRequest httpServletRequest,
        List<ApiErrorDetail> validationErrors
) {

    public ExceptionContext {
        validationErrors = validationErrors != null ? List.copyOf(validationErrors) : List.of();
    }

    public static ExceptionContext of(final Exception exception, final ErrorCode errorCode, final String errorDetailMsg, final CurrentAccountDTO account, final HttpServletRequest httpServletRequest) {
        return new ExceptionContext(exception, errorCode, errorDetailMsg, account, httpServletRequest, List.of());
    }

    public static ExceptionContext of(final Exception exception, final ErrorCode errorCode, final String errorDetailMsg, final CurrentAccountDTO account, final HttpServletRequest httpServletRequest, final List<ApiErrorDetail> validationErrors) {
        return new ExceptionContext(exception, errorCode, errorDetailMsg, account, httpServletRequest, validationErrors);
    }

    public static ExceptionContext of(final Exception exception, final CurrentAccountDTO account, final HttpServletRequest httpServletRequest) {
        return new ExceptionContext(exception, null, null, account, httpServletRequest, List.of());
    }
}
