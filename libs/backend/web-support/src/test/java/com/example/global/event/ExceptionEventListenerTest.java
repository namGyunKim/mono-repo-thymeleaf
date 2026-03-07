package com.example.global.event;

import com.example.domain.log.event.ExceptionEvent;
import com.example.global.exception.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link ExceptionEventListener} 단위 테스트
 *
 * <p>이벤트 수신 시 로그 기록 동작과 null Guard Clause를 검증한다.</p>
 */
@ExtendWith(MockitoExtension.class)
class ExceptionEventListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    // --- onExceptionEventCommitted ---

    @Test
    void onExceptionEventCommitted_null_does_not_throw() {
        final ExceptionEventListener listener = createListener(false);

        assertThatCode(() -> listener.onExceptionEventCommitted(null))
                .doesNotThrowAnyException();
    }

    @Test
    void onExceptionEventCommitted_valid_event_logs_without_exception() {
        final ExceptionEventListener listener = createListener(false);
        final ExceptionEvent event = createTestEvent();

        assertThatCode(() -> listener.onExceptionEventCommitted(event))
                .doesNotThrowAnyException();
    }

    @Test
    void onExceptionEventCommitted_structured_logging_enabled_logs_without_exception() throws Exception {
        final ExceptionEventListener listener = createListener(true);
        final ExceptionEvent event = createTestEvent();

        given(objectMapper.writeValueAsString(anyMap())).willReturn("{\"test\":\"value\"}");

        assertThatCode(() -> listener.onExceptionEventCommitted(event))
                .doesNotThrowAnyException();

        verify(objectMapper).writeValueAsString(anyMap());
    }

    // --- onExceptionEventRolledBack ---

    @Test
    void onExceptionEventRolledBack_null_does_not_throw() {
        final ExceptionEventListener listener = createListener(false);

        assertThatCode(() -> listener.onExceptionEventRolledBack(null))
                .doesNotThrowAnyException();
    }

    @Test
    void onExceptionEventRolledBack_valid_event_logs_without_exception() {
        final ExceptionEventListener listener = createListener(false);
        final ExceptionEvent event = createTestEvent();

        assertThatCode(() -> listener.onExceptionEventRolledBack(event))
                .doesNotThrowAnyException();
    }

    // --- 구조화 로깅 실패 시 ---

    @Test
    void onExceptionEventCommitted_serialization_failure_does_not_propagate() throws Exception {
        final ExceptionEventListener listener = createListener(true);
        final ExceptionEvent event = createTestEvent();

        given(objectMapper.writeValueAsString(anyMap()))
                .willThrow(new TestJacksonException("직렬화 실패"));

        assertThatCode(() -> listener.onExceptionEventCommitted(event))
                .doesNotThrowAnyException();
    }

    @Test
    void onExceptionEventCommitted_structured_logging_disabled_skips_object_mapper() throws Exception {
        final ExceptionEventListener listener = createListener(false);
        final ExceptionEvent event = createTestEvent();

        assertThatCode(() -> listener.onExceptionEventCommitted(event))
                .doesNotThrowAnyException();

        verify(objectMapper, never()).writeValueAsString(anyMap());
    }

    // --- 헬퍼 메서드 ---

    private ExceptionEventListener createListener(final boolean structuredEnabled) {
        return new ExceptionEventListener(objectMapper, structuredEnabled);
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
                java.util.List.of()
        );
    }

    /**
     * 테스트 전용 JacksonException 구현
     */
    private static final class TestJacksonException extends JacksonException {
        TestJacksonException(final String message) {
            super(message);
        }
    }
}
