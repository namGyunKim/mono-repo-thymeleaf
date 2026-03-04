package com.example.global.payload.response;

import com.example.global.entity.BaseTimeEntity;
import com.example.global.utils.DateTimeFormatUtils;

/**
 * 감사(Audit) 정보 공통 응답 DTO
 *
 * <p>
 * BaseTimeEntity를 상속하는 엔티티의 생성/수정 정보를 구조화하여 전달한다.
 * </p>
 */
public record AuditInfoResponse(
        String createdAt,
        String createdBy,
        String modifiedAt,
        String modifiedBy
) {

    public static AuditInfoResponse from(final BaseTimeEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity는 필수입니다.");
        }
        return new AuditInfoResponse(
                DateTimeFormatUtils.formatKoreanDateTime(entity.getCreatedAt()),
                entity.getCreatedBy(),
                DateTimeFormatUtils.formatKoreanDateTime(entity.getModifiedAt()),
                entity.getLastModifiedBy()
        );
    }
}
