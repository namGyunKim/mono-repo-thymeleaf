package com.example.domain.log.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ExceptionLog 복합 기본키 ({@code id + createdAt})
 *
 * <p>
 * JPA {@code @IdClass}이므로 기본 생성자가 필요하여 {@code final} 필드를 사용할 수 없습니다.
 * 대신 setter를 제공하지 않고 {@code @Getter}만 노출하여 외부 불변성을 보장합니다.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ExceptionLogId implements Serializable {

    private Long id;
    private LocalDateTime createdAt;

    public ExceptionLogId(final Long id, final LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
