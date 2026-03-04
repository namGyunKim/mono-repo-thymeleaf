package com.example.global.exception.advice.support;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.security.guard.MemberGuard;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ApiErrorResponseFactory;
import com.example.global.exception.support.ApiVersionErrorResolver;
import com.example.global.exception.support.ExceptionEventPublisher;
import com.example.global.exception.support.ExceptionMessageResolver;
import com.example.global.exception.support.FilterLoggingMarker;
import com.example.global.exception.support.HttpStatusResolver;
import com.example.global.exception.support.ValidationErrorMapper;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.List;

/**
 * 예외 처리(ControllerAdvice)에서 공통으로 사용하는 기능을 Facade로 모은 클래스
 * - 응답 생성, 메시지 해석, 이벤트 발행 등의 협력 객체를 한곳에서 관리합니다.
 */
@Component
@RequiredArgsConstructor
public class ExceptionAdviceSupport {

    // --- 응답 생성 및 메시지 해석 ---
    private final ApiErrorResponseFactory apiErrorResponseFactory;
    private final HttpStatusResolver httpStatusResolver;
    private final ApiVersionErrorResolver apiVersionErrorResolver;
    private final ValidationErrorMapper validationErrorMapper;
    private final ExceptionMessageResolver exceptionMessageResolver;

    // --- 이벤트 발행 및 로깅 ---
    private final ExceptionEventPublisher exceptionEventPublisher;
    private final FilterLoggingMarker filterLoggingMarker;

    // --- 인증/인가 ---
    private final MemberGuard memberGuard;

    public <T> T withFilterLogged(final HttpServletRequest request, final Supplier<T> action) {
        markFilterLogged(request);
        return action.get();
    }

    public void markFilterLogged(final HttpServletRequest request) {
        filterLoggingMarker.markFilterLogged(request);
    }

    public void publishExceptionEvent(final ExceptionEvent event) {
        exceptionEventPublisher.publish(event);
    }

    public CurrentAccountDTO resolveAccount(final CurrentAccountDTO account) {
        if (account != null) {
            return account;
        }
        return memberGuard.getCurrentAccountOrGuest();
    }

    public ResponseEntity<ApiErrorResponse> toResponse(final ErrorCode errorCode, final HttpStatus status) {
        return apiErrorResponseFactory.toResponse(errorCode, status);
    }

    public ResponseEntity<ApiErrorResponse> toResponse(final ErrorCode errorCode, final HttpStatus status, final List<ApiErrorDetail> errors) {
        return apiErrorResponseFactory.toResponse(errorCode, status, errors);
    }

    public HttpStatus resolveHttpStatus(final ErrorCode errorCode) {
        return httpStatusResolver.resolve(errorCode);
    }

    public String resolveMessage(final Exception e, final String fallback) {
        return exceptionMessageResolver.resolveMessage(e, fallback);
    }

    public ErrorCode resolveApiVersionErrorCode(final HttpServletRequest request, final ErrorCode fallback) {
        return apiVersionErrorResolver.resolve(request, fallback);
    }

    public String resolveDetailMessage(final Exception e, final ErrorCode errorCode) {
        return exceptionMessageResolver.resolveDetailMessage(e, errorCode);
    }

    public List<ApiErrorDetail> resolveValidationErrors(final Exception e) {
        return validationErrorMapper.resolveValidationErrors(e);
    }

    public String resolveValidationDetailMessage(final Exception e, final String fallback) {
        return validationErrorMapper.resolveValidationDetailMessage(e, fallback);
    }
}
