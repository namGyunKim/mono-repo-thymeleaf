package com.example.global.exception.advice;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionContext;
import com.example.domain.log.event.ExceptionEvent;
import com.example.global.annotation.CurrentAccount;
import com.example.global.exception.advice.support.ExceptionAdviceSupport;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogWriter;
import com.example.global.logging.RequestMeta;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RestControllerAdvice
@RequiredArgsConstructor
public class BadRequestExceptionAdvice {

    private final ExceptionAdviceSupport support;
    private final ExceptionLogWriter exceptionLogWriter;

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            final MethodArgumentTypeMismatchException e,
            @CurrentAccount final CurrentAccountDTO account,
            final HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logTypeMismatch(meta, e, ErrorCode.INVALID_PARAMETER);
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    ErrorCode.INVALID_PARAMETER,
                    support.resolveMessage(e, ErrorCode.INVALID_PARAMETER.getErrorMessage()),
                    resolvedAccount,
                    request
            ));
            return support.toResponse(ErrorCode.INVALID_PARAMETER, HttpStatus.BAD_REQUEST);
        });
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            final MissingServletRequestParameterException e,
            @CurrentAccount final CurrentAccountDTO account,
            final HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logMissingParameter(meta, e, ErrorCode.INVALID_PARAMETER);
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    ErrorCode.INVALID_PARAMETER,
                    support.resolveMessage(e, ErrorCode.INVALID_PARAMETER.getErrorMessage()),
                    resolvedAccount,
                    request
            ));
            return support.toResponse(ErrorCode.INVALID_PARAMETER, HttpStatus.BAD_REQUEST);
        });
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            final HttpMessageNotReadableException e,
            @CurrentAccount final CurrentAccountDTO account,
            final HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logMessageOnly(meta, ErrorCode.INVALID_PARAMETER, "요청 본문(JSON) 파싱 실패");
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    ErrorCode.INVALID_PARAMETER,
                    "요청 본문(JSON) 파싱 실패",
                    resolvedAccount,
                    request
            ));
            return support.toResponse(ErrorCode.INVALID_PARAMETER, HttpStatus.BAD_REQUEST);
        });
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            final Exception e,
            @CurrentAccount final CurrentAccountDTO account,
            final HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logMessageOnly(meta, ErrorCode.INPUT_VALUE_INVALID, "요청 값 검증 실패");
            final String detailMessage = support.resolveValidationDetailMessage(e, "요청 값 검증 실패");
            final List<ApiErrorDetail> validationErrors = support.resolveValidationErrors(e);
            support.publishExceptionEvent(ExceptionEvent.from(
                    ExceptionContext.of(e, ErrorCode.INPUT_VALUE_INVALID, detailMessage, resolvedAccount, request, validationErrors)
            ));
            return support.toResponse(
                    ErrorCode.INPUT_VALUE_INVALID,
                    HttpStatus.BAD_REQUEST,
                    validationErrors
            );
        });
    }
}
