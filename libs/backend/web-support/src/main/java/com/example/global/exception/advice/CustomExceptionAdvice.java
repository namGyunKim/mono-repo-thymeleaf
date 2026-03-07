package com.example.global.exception.advice;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.global.annotation.CurrentAccount;
import com.example.global.exception.GlobalException;
import com.example.global.exception.SocialException;
import com.example.global.exception.advice.support.ExceptionAdviceSupport;
import com.example.global.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class CustomExceptionAdvice {

    private final ExceptionAdviceSupport support;

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            final GlobalException e,
            @CurrentAccount final CurrentAccountDTO account,
            final HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            support.publishExceptionEvent(ExceptionEvent.from((Exception) e, resolvedAccount, request));
            final HttpStatus status = support.resolveHttpStatus(e.getErrorCode());
            return support.toResponse(e.getErrorCode(), status);
        });
    }

    @ExceptionHandler(SocialException.class)
    public ResponseEntity<ApiErrorResponse> handleSocialException(
            final SocialException e,
            @CurrentAccount final CurrentAccountDTO account,
            final HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            support.publishExceptionEvent(ExceptionEvent.from((Exception) e, resolvedAccount, request));
            return support.toResponse(e.getErrorCode(), HttpStatus.BAD_REQUEST);
        });
    }

}
