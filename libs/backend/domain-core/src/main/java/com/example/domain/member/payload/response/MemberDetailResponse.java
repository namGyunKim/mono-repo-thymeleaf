package com.example.domain.member.payload.response;

import com.example.domain.member.entity.Member;
import com.example.global.payload.response.AuditInfoResponse;

/**
 * 회원 상세 응답
 *
 * <p>
 * 프로필 정보({@link MemberProfileListResponse})와
 * 감사 정보({@link AuditInfoResponse})를 조합하여 구성한다.
 * </p>
 */
public record MemberDetailResponse(
        MemberProfileListResponse profile,
        AuditInfoResponse audit
) {

    public static MemberDetailResponse from(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberDetailResponse(
                MemberProfileListResponse.from(member),
                AuditInfoResponse.from(member)
        );
    }
}
