package com.example.domain.log.entity;

import com.example.domain.log.payload.dto.ExceptionLogCreateCommand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 예외 로그 전용 불변 엔티티
 *
 * <p>
 * 모든 예외(일반 예외, 바인딩/검증 에러 포함)를 기록한다.
 * 생성 후 수정/삭제 메서드를 제공하지 않으며, 감사 추적 목적의 불변 기록이다.
 * </p>
 *
 * <p>
 * 필드 구조 — WHO(누가) / WHY(왜) / HOW(어떻게)
 * </p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exception_log", comment = "예외 로그")
@IdClass(ExceptionLogId.class)
@EntityListeners(value = {AuditingEntityListener.class})
public class ExceptionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exception_log_id_seq")
    @SequenceGenerator(
            name = "exception_log_id_seq",
            sequenceName = "exception_log_id_seq",
            allocationSize = 1
    )
    @Column(name = "log_id", comment = "로그 아이디")
    private Long id;

    @Id
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false, comment = "생성일")
    private LocalDateTime createdAt;

    // --- WHO (누가) ---

    @Column(length = 100, comment = "요청자 로그인 아이디")
    private String loginId;

    @Column(comment = "요청자 회원 ID")
    private Long memberId;

    @Column(length = 50, comment = "요청자 역할")
    private String accountRole;

    @Column(length = 45, comment = "요청 클라이언트 IP")
    private String clientIp;

    // --- WHY (왜) ---

    @Column(length = 100, comment = "예외 클래스 이름")
    private String errorName;

    @Column(length = 20, comment = "에러 코드")
    private String errorCode;

    @Column(length = 500, comment = "에러 상세 메시지")
    private String errorDetailMessage;

    @Column(columnDefinition = "text", comment = "디버그 스택 트레이스")
    private String debugStackTrace;

    @Column(columnDefinition = "text", comment = "바인딩/검증 에러 상세 (JSON)")
    private String validationErrors;

    // --- HOW (어떻게) ---

    @Column(length = 36, comment = "추적 ID")
    private String traceId;

    @Column(length = 500, comment = "요청 경로")
    private String requestPath;

    @Column(length = 10, comment = "요청 HTTP 메서드")
    private String requestMethod;

    private ExceptionLog(
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
        this.traceId = traceId;
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
        this.errorName = errorName;
        this.errorCode = errorCode;
        this.errorDetailMessage = errorDetailMessage;
        this.debugStackTrace = debugStackTrace;
        this.validationErrors = validationErrors;
        this.loginId = loginId;
        this.memberId = memberId;
        this.accountRole = accountRole;
        this.clientIp = clientIp;
    }

    /**
     * 예외 로그 생성 팩토리
     *
     * @param command 예외 로그 생성 커맨드 (null 불가)
     * @return 예외 로그 엔티티
     * @throws IllegalArgumentException command가 null인 경우
     */
    public static ExceptionLog from(final ExceptionLogCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command는 필수입니다.");
        }
        return new ExceptionLog(
                command.traceId(),
                command.requestPath(),
                command.requestMethod(),
                command.errorName(),
                command.errorCode(),
                command.errorDetailMessage(),
                command.debugStackTrace(),
                command.validationErrors(),
                command.loginId(),
                command.memberId(),
                command.accountRole(),
                command.clientIp()
        );
    }
}
