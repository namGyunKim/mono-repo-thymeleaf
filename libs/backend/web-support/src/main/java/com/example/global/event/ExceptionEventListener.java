package com.example.global.event;

import com.example.domain.log.event.ExceptionEvent;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.EventLogTemplates;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class ExceptionEventListener {

    private static final String TX_STATUS_COMMITTED = "COMMITTED";
    private static final String TX_STATUS_ROLLED_BACK = "ROLLED_BACK";

    private final ObjectMapper objectMapper;
    private final boolean structuredExceptionLoggingEnabled;

    public ExceptionEventListener(
            final ObjectMapper objectMapper,
            @Value("${app.logging.exception.structured-enabled:false}") final boolean structuredExceptionLoggingEnabled
    ) {
        this.objectMapper = objectMapper;
        this.structuredExceptionLoggingEnabled = structuredExceptionLoggingEnabled;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onExceptionEventCommitted(final ExceptionEvent exceptionEvent) {
        if (exceptionEvent == null) {
            return;
        }
        logExceptionEvent(exceptionEvent, TX_STATUS_COMMITTED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onExceptionEventRolledBack(final ExceptionEvent exceptionEvent) {
        if (exceptionEvent == null) {
            return;
        }
        logExceptionEvent(exceptionEvent, TX_STATUS_ROLLED_BACK);
    }

    private void logExceptionEvent(final ExceptionEvent exceptionEvent, final String txStatus) {
        if (exceptionEvent == null) {
            return;
        }
        final String resolvedTxStatus = resolveTxStatus(txStatus);

        log.error(
                EventLogTemplates.EXCEPTION_EVENT_LOG_TEMPLATE.stripTrailing(),
                resolvedTxStatus,
                exceptionEvent.toLogString()
        );

        if (structuredExceptionLoggingEnabled) {
            final String structuredPayload = toStructuredPayload(exceptionEvent, resolvedTxStatus);
            if (structuredPayload != null) {
                log.error(
                        EventLogTemplates.EXCEPTION_EVENT_STRUCTURED_LOG_TEMPLATE.stripTrailing(),
                        resolvedTxStatus,
                        structuredPayload
                );
            }
        }
    }

    private String toStructuredPayload(final ExceptionEvent exceptionEvent, final String txStatus) {
        try {
            final Map<String, Object> payload = new LinkedHashMap<>(exceptionEvent.getStructuredLog());
            payload.put("txStatus", txStatus);
            return objectMapper.writeValueAsString(payload);
        } catch (final JacksonException e) {
            log.warn(
                    EventLogTemplates.EXCEPTION_EVENT_STRUCTURED_FAIL_LOG_TEMPLATE.stripTrailing(),
                    txStatus,
                    ErrorCode.FAILED.getCode(),
                    e.getClass().getSimpleName(),
                    e.getMessage() != null ? e.getMessage() : "UNKNOWN"
            );
            return null;
        }
    }

    private String resolveTxStatus(final String txStatus) {
        if (txStatus == null || txStatus.isBlank()) {
            return TX_STATUS_COMMITTED;
        }
        return txStatus;
    }
}
