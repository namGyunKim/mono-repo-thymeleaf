package com.example.domain.log.payload.response;

import com.example.domain.log.payload.dto.MemberLogView;

/**
 * 로그 행위자 정보 (대상 회원 ID, 수행자 ID)
 */
public record LogActorResponse(
        String subjectLoginId,
        String executorLoginId
) {

    public static LogActorResponse from(final MemberLogView view) {
        if (view == null) {
            throw new IllegalArgumentException("view는 필수입니다.");
        }
        return new LogActorResponse(view.loginId(), view.executorId());
    }

    public static LogActorResponse of(final String subjectLoginId, final String executorLoginId) {
        return new LogActorResponse(subjectLoginId, executorLoginId);
    }
}
