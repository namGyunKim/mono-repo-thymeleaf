package com.example.domain.log.payload.dto;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionLogCreateCommandTest {

    @Test
    void from_valid_event_creates_command() {
        final CurrentAccountDTO account = CurrentAccountDTO.ofGuest();
        final ExceptionEvent event = ExceptionEvent.of(
                "trace-123",
                "/api/test",
                "GET",
                "GlobalException",
                ErrorCode.INTERNAL_SERVER_ERROR,
                "내부 서버 오류",
                null,
                account,
                LocalDateTime.of(2025, 6, 1, 12, 0),
                "127.0.0.1",
                List.of()
        );

        final ExceptionLogCreateCommand result = ExceptionLogCreateCommand.from(event);

        assertThat(result.traceId()).isEqualTo("trace-123");
        assertThat(result.requestPath()).isEqualTo("/api/test");
        assertThat(result.requestMethod()).isEqualTo("GET");
        assertThat(result.errorName()).isEqualTo("GlobalException");
        assertThat(result.errorCode()).isEqualTo("0004");
        assertThat(result.errorDetailMessage()).isEqualTo("내부 서버 오류");
        assertThat(result.debugStackTrace()).isNull();
        assertThat(result.validationErrors()).isNull();
        assertThat(result.loginId()).isEqualTo("GUEST");
        assertThat(result.memberId()).isEqualTo(0L);
        assertThat(result.accountRole()).isEqualTo("GUEST");
        assertThat(result.clientIp()).isEqualTo("127.0.0.1");
    }

    @Test
    void from_event_with_null_account_sets_null_fields() {
        final ExceptionEvent event = ExceptionEvent.of(
                "trace-456",
                "/api/test",
                "POST",
                "NullPointerException",
                ErrorCode.FAILED,
                "예상치 못한 오류",
                null,
                null,
                LocalDateTime.of(2025, 6, 1, 12, 0),
                "10.0.0.1",
                List.of()
        );

        final ExceptionLogCreateCommand result = ExceptionLogCreateCommand.from(event);

        assertThat(result.loginId()).isNull();
        assertThat(result.memberId()).isNull();
        assertThat(result.accountRole()).isNull();
    }

    @Test
    void from_event_with_validation_errors_serializes_json() {
        final List<ApiErrorDetail> validationErrors = List.of(
                ApiErrorDetail.of("email", "필수 입력"),
                ApiErrorDetail.of("name", "빈 값 불가")
        );
        final ExceptionEvent event = ExceptionEvent.of(
                "trace-789",
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

        final ExceptionLogCreateCommand result = ExceptionLogCreateCommand.from(event);

        assertThat(result.validationErrors()).contains("email");
        assertThat(result.validationErrors()).contains("필수 입력");
        assertThat(result.validationErrors()).contains("name");
        assertThat(result.validationErrors()).contains("빈 값 불가");
    }

    @Test
    void from_event_with_empty_validation_errors_returns_null() {
        final ExceptionEvent event = ExceptionEvent.of(
                "trace-000",
                "/api/test",
                "GET",
                "RuntimeException",
                ErrorCode.FAILED,
                "오류",
                null,
                null,
                LocalDateTime.of(2025, 6, 1, 12, 0),
                "127.0.0.1",
                List.of()
        );

        final ExceptionLogCreateCommand result = ExceptionLogCreateCommand.from(event);

        assertThat(result.validationErrors()).isNull();
    }

    @Test
    void from_null_event_throws_IllegalArgumentException() {
        assertThatThrownBy(() -> ExceptionLogCreateCommand.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("event는 필수입니다.");
    }
}
