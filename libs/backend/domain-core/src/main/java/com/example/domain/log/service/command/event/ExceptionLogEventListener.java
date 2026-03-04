package com.example.domain.log.service.command.event;

import com.example.domain.log.entity.ExceptionLog;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.log.payload.dto.ExceptionLogCreateCommand;
import com.example.domain.log.repository.ExceptionLogRepository;
import com.example.global.exception.BaseAppException;
import com.example.global.exception.enums.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 예외 이벤트를 수신하여 {@code exception_log} 테이블에 저장하는 리스너
 *
 * <p>
 * 인증 여부와 무관하게 모든 예외를 기록한다.
 * 저장 실패 시 예외를 전파하지 않고 SLF4J로 fallback 로깅한다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionLogEventListener {

    private final ExceptionLogRepository exceptionLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleExceptionEvent(final ExceptionEvent event) {
        if (event == null) {
            return;
        }
        try {
            final ExceptionLogCreateCommand command = ExceptionLogCreateCommand.from(event);
            final ExceptionLog logEntity = ExceptionLog.from(command);
            exceptionLogRepository.save(logEntity);

            log.debug(
                    "예외 로그 저장 완료: traceId={}, errorName={}",
                    command.traceId(),
                    command.errorName()
            );
        } catch (final Exception e) {
            log.error(
                    "errorCode={}, 예외 로그 저장 중 오류 발생: exceptionName={}, traceId={}, message={}",
                    resolveErrorCode(e),
                    e.getClass().getSimpleName(),
                    event.traceId(),
                    e.getMessage(),
                    e
            );
        }
    }

    private String resolveErrorCode(final Exception exception) {
        if (exception instanceof BaseAppException baseAppException
                && baseAppException.getErrorCode() != null) {
            return baseAppException.getErrorCode().getCode();
        }
        return ErrorCode.FAILED.getCode();
    }
}
