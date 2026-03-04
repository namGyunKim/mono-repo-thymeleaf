package com.example.domain.log.payload.dto;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.log.support.ValidationErrorSerializer;

/**
 * 예외 로그 엔티티 생성용 Command DTO
 *
 * <p>
 * {@link ExceptionEvent}로부터 DB 저장에 필요한 값을 추출하여 보관한다.
 * </p>
 */
public record ExceptionLogCreateCommand(
        String traceId,
        String requestPath,
        String requestMethod,
        String errorName,
        String errorCode,
        String errorDetailMessage,
        String debugStackTrace,
        String validationErrors,
        String loginId,
        Long memberId,
        String accountRole,
        String clientIp
) {

    public static ExceptionLogCreateCommand of(
            final String traceId,
            final String requestPath,
            final String requestMethod,
            final String errorName,
            final String errorCode,
            final String errorDetailMessage,
            final String debugStackTrace,
            final String validationErrors,
            final String loginId,
            final Long memberId,
            final String accountRole,
            final String clientIp
    ) {
        return new ExceptionLogCreateCommand(traceId, requestPath, requestMethod, errorName, errorCode,
                errorDetailMessage, debugStackTrace, validationErrors, loginId, memberId, accountRole, clientIp);
    }

    /**
     * ExceptionEvent로부터 Command를 생성한다.
     *
     * @param event 예외 이벤트 (null 불가)
     * @return 예외 로그 생성 커맨드
     * @throws IllegalArgumentException event가 null인 경우
     */
    public static ExceptionLogCreateCommand from(final ExceptionEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event는 필수입니다.");
        }
        final CurrentAccountDTO account = event.account();
        return new ExceptionLogCreateCommand(
                event.traceId(),
                event.requestPath(),
                event.requestMethod(),
                event.errorName(),
                event.errorCode() != null ? event.errorCode().getCode() : null,
                event.errorDetailMsg(),
                event.debugStackTrace(),
                ValidationErrorSerializer.serialize(event.validationErrors()),
                account != null ? account.loginId() : null,
                account != null ? account.id() : null,
                account != null && account.role() != null ? account.role().name() : null,
                event.clientIp()
        );
    }
}
