package com.example.global.exception.advice;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.global.exception.advice.support.ExceptionAdviceSupport;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.GlobalException;
import com.example.global.exception.SocialException;
import com.example.global.payload.response.ApiErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.function.Supplier;

/**
 * {@link CustomExceptionAdvice} 단위 테스트
 *
 * <p>예외별 올바른 HTTP 상태 코드와 에러 코드 반환을 검증한다.</p>
 */
@ExtendWith(MockitoExtension.class)
class CustomExceptionAdviceTest {

    @Mock
    private ExceptionAdviceSupport support;

    @InjectMocks
    private CustomExceptionAdvice advice;

    // --- handleGlobalException ---

    @Test
    void handleGlobalException_global_exception_returns_matching_http_status() {
        // given
        final GlobalException exception = new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final CurrentAccountDTO account = CurrentAccountDTO.ofGuest();

        final ResponseEntity<ApiErrorResponse> expectedResponse =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR));

        stubWithFilterLogged(expectedResponse);
        given(support.resolveAccount(account)).willReturn(account);
        given(support.resolveHttpStatus(ErrorCode.INTERNAL_SERVER_ERROR)).willReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        given(support.toResponse(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
                .willReturn(expectedResponse);

        // when
        final ResponseEntity<ApiErrorResponse> result = advice.handleGlobalException(exception, account, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().code()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }

    @Test
    void handleGlobalException_bad_request_error_code_returns_400() {
        // given
        final GlobalException exception = new GlobalException(ErrorCode.INPUT_VALUE_INVALID);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final CurrentAccountDTO account = CurrentAccountDTO.ofGuest();

        final ResponseEntity<ApiErrorResponse> expectedResponse =
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiErrorResponse.from(ErrorCode.INPUT_VALUE_INVALID));

        stubWithFilterLogged(expectedResponse);
        given(support.resolveAccount(account)).willReturn(account);
        given(support.resolveHttpStatus(ErrorCode.INPUT_VALUE_INVALID)).willReturn(HttpStatus.BAD_REQUEST);
        given(support.toResponse(ErrorCode.INPUT_VALUE_INVALID, HttpStatus.BAD_REQUEST))
                .willReturn(expectedResponse);

        // when
        final ResponseEntity<ApiErrorResponse> result = advice.handleGlobalException(exception, account, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().code()).isEqualTo(ErrorCode.INPUT_VALUE_INVALID.getCode());
    }

    // --- handleSocialException ---

    @Test
    void handleSocialException_social_exception_always_returns_bad_request() {
        // given
        final SocialException exception = new SocialException(ErrorCode.SOCIAL_TOKEN_ERROR);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final CurrentAccountDTO account = CurrentAccountDTO.ofGuest();

        final ResponseEntity<ApiErrorResponse> expectedResponse =
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiErrorResponse.from(ErrorCode.SOCIAL_TOKEN_ERROR));

        stubWithFilterLogged(expectedResponse);
        given(support.resolveAccount(account)).willReturn(account);
        given(support.toResponse(ErrorCode.SOCIAL_TOKEN_ERROR, HttpStatus.BAD_REQUEST))
                .willReturn(expectedResponse);

        // when
        final ResponseEntity<ApiErrorResponse> result = advice.handleSocialException(exception, account, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().code()).isEqualTo(ErrorCode.SOCIAL_TOKEN_ERROR.getCode());
    }

    @Test
    void handleGlobalException_publishes_exception_event() {
        // given
        final GlobalException exception = new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final CurrentAccountDTO account = CurrentAccountDTO.ofGuest();

        final ResponseEntity<ApiErrorResponse> expectedResponse =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR));

        stubWithFilterLogged(expectedResponse);
        given(support.resolveAccount(account)).willReturn(account);
        given(support.resolveHttpStatus(ErrorCode.INTERNAL_SERVER_ERROR)).willReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        given(support.toResponse(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR))
                .willReturn(expectedResponse);

        // when
        advice.handleGlobalException(exception, account, request);

        // then
        verify(support).publishExceptionEvent(any(ExceptionEvent.class));
    }

    // --- 헬퍼 메서드 ---

    /**
     * withFilterLogged가 supplier를 실행하고 그 결과를 반환하도록 설정한다.
     */
    @SuppressWarnings("unchecked")
    private void stubWithFilterLogged(final ResponseEntity<ApiErrorResponse> expectedResponse) {
        given(support.withFilterLogged(any(), any(Supplier.class)))
                .willAnswer(invocation -> {
                    final Supplier<ResponseEntity<ApiErrorResponse>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
    }
}
