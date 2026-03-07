package com.example.domain.log.service.command.event;

import com.example.domain.log.entity.ExceptionLog;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.log.repository.ExceptionLogRepository;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExceptionLogEventListenerTest {

    @Mock
    private ExceptionLogRepository exceptionLogRepository;

    @InjectMocks
    private ExceptionLogEventListener listener;

    @Test
    void handleExceptionEvent_valid_event_saves_log() {
        final ExceptionEvent event = createTestEvent();

        listener.handleExceptionEvent(event);

        final ArgumentCaptor<ExceptionLog> captor = ArgumentCaptor.forClass(ExceptionLog.class);
        verify(exceptionLogRepository).save(captor.capture());

        final ExceptionLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isEqualTo("test-trace-id");
        assertThat(saved.getErrorName()).isEqualTo("TestException");
        assertThat(saved.getErrorCode()).isEqualTo("0004");
        assertThat(saved.getRequestPath()).isEqualTo("/api/test");
        assertThat(saved.getRequestMethod()).isEqualTo("GET");
        assertThat(saved.getClientIp()).isEqualTo("127.0.0.1");
    }

    @Test
    void handleExceptionEvent_null_event_does_not_save() {
        listener.handleExceptionEvent(null);

        verify(exceptionLogRepository, never()).save(any());
    }

    @Test
    void handleExceptionEvent_save_failure_does_not_propagate() {
        final ExceptionEvent event = createTestEvent();
        given(exceptionLogRepository.save(any())).willThrow(new RuntimeException("DB 연결 실패"));

        assertThatCode(() -> listener.handleExceptionEvent(event))
                .doesNotThrowAnyException();
    }

    @Test
    void handleExceptionEvent_null_account_saves_with_null_user_fields() {
        final ExceptionEvent event = ExceptionEvent.of(
                "trace-no-auth",
                "/api/public",
                "GET",
                "NoHandlerFoundException",
                ErrorCode.PAGE_NOT_EXIST,
                "페이지를 찾을 수 없습니다.",
                null,
                null,
                LocalDateTime.of(2025, 6, 1, 12, 0),
                "10.0.0.1",
                List.of()
        );

        listener.handleExceptionEvent(event);

        final ArgumentCaptor<ExceptionLog> captor = ArgumentCaptor.forClass(ExceptionLog.class);
        verify(exceptionLogRepository).save(captor.capture());

        final ExceptionLog saved = captor.getValue();
        assertThat(saved.getLoginId()).isNull();
        assertThat(saved.getMemberId()).isNull();
        assertThat(saved.getAccountRole()).isNull();
    }

    @Test
    void handleExceptionEvent_with_validation_errors_saves_json() {
        final List<ApiErrorDetail> validationErrors = List.of(
                ApiErrorDetail.of("email", "필수 입력"),
                ApiErrorDetail.of("name", "빈 값 불가")
        );
        final ExceptionEvent event = ExceptionEvent.of(
                "trace-validation",
                "/api/members",
                "POST",
                "MethodArgumentNotValidException",
                ErrorCode.INPUT_VALUE_INVALID,
                "요청 값 검증 실패",
                null,
                null,
                LocalDateTime.of(2025, 6, 1, 12, 0),
                "127.0.0.1",
                validationErrors
        );

        listener.handleExceptionEvent(event);

        final ArgumentCaptor<ExceptionLog> captor = ArgumentCaptor.forClass(ExceptionLog.class);
        verify(exceptionLogRepository).save(captor.capture());

        final ExceptionLog saved = captor.getValue();
        assertThat(saved.getValidationErrors()).contains("email");
        assertThat(saved.getValidationErrors()).contains("필수 입력");
    }

    private ExceptionEvent createTestEvent() {
        return ExceptionEvent.of(
                "test-trace-id",
                "/api/test",
                "GET",
                "TestException",
                ErrorCode.INTERNAL_SERVER_ERROR,
                "테스트 에러 메시지",
                null,
                null,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                "127.0.0.1",
                List.of()
        );
    }
}
