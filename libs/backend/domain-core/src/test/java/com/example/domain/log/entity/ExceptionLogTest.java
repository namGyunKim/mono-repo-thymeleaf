package com.example.domain.log.entity;

import com.example.domain.log.payload.dto.ExceptionLogCreateCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionLogTest {

    @Test
    void from_valid_command_creates_entity() {
        final ExceptionLogCreateCommand command = ExceptionLogCreateCommand.of(
                "trace-123",
                "/api/members",
                "POST",
                "GlobalException",
                "1101",
                "존재하지 않는 회원입니다.",
                null,
                null,
                "testuser",
                1L,
                "USER",
                "127.0.0.1"
        );

        final ExceptionLog result = ExceptionLog.from(command);

        assertThat(result.getTraceId()).isEqualTo("trace-123");
        assertThat(result.getRequestPath()).isEqualTo("/api/members");
        assertThat(result.getRequestMethod()).isEqualTo("POST");
        assertThat(result.getErrorName()).isEqualTo("GlobalException");
        assertThat(result.getErrorCode()).isEqualTo("1101");
        assertThat(result.getErrorDetailMessage()).isEqualTo("존재하지 않는 회원입니다.");
        assertThat(result.getDebugStackTrace()).isNull();
        assertThat(result.getValidationErrors()).isNull();
        assertThat(result.getLoginId()).isEqualTo("testuser");
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getAccountRole()).isEqualTo("USER");
        assertThat(result.getClientIp()).isEqualTo("127.0.0.1");
    }

    @Test
    void from_command_with_validation_errors_creates_entity() {
        final String validationJson = "[{\"field\":\"email\",\"reason\":\"필수\"}]";
        final ExceptionLogCreateCommand command = ExceptionLogCreateCommand.of(
                "trace-456",
                "/api/members",
                "POST",
                "MethodArgumentNotValidException",
                "0001",
                "요청 값 검증 실패",
                null,
                validationJson,
                "admin",
                2L,
                "ADMIN",
                "192.168.1.1"
        );

        final ExceptionLog result = ExceptionLog.from(command);

        assertThat(result.getValidationErrors()).isEqualTo(validationJson);
        assertThat(result.getErrorName()).isEqualTo("MethodArgumentNotValidException");
    }

    @Test
    void from_null_command_throws_IllegalArgumentException() {
        assertThatThrownBy(() -> ExceptionLog.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("command는 필수입니다.");
    }
}
