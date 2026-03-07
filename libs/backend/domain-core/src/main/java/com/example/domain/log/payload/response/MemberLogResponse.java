package com.example.domain.log.payload.response;

import com.example.domain.log.payload.dto.MemberLogView;

/**
 * 회원 활동 로그 응답 — 로그 ID + 행위자 + 이벤트 상세로 구성
 */
public record MemberLogResponse(
        Long id,
        LogActorResponse actor,
        LogEventResponse event
) {

    public static MemberLogResponse from(final MemberLogView view) {
        if (view == null) {
            throw new IllegalArgumentException("view는 필수입니다.");
        }
        return new MemberLogResponse(
                view.id(),
                LogActorResponse.from(view),
                LogEventResponse.from(view)
        );
    }
}
