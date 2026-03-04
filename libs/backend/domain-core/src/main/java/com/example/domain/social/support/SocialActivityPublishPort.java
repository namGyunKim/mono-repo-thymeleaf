package com.example.domain.social.support;

import com.example.domain.log.enums.LogType;

/**
 * social 도메인에서 log 도메인으로 활동 로그를 발행하는 포트.
 *
 * <p>방향: social → log
 *
 * <p>GoogleSocialActivityPublisher가 LogActivityPublisher / MemberActivityCommand에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface SocialActivityPublishPort {

    /**
     * 소셜 로그인 관련 회원 활동 로그를 발행한다.
     *
     * <p>소셜 로그인 성공, 신규 소셜 회원 가입 등의 활동을 기록할 때 사용한다.
     *
     * @param loginId  null이 아닌 비어 있지 않은 대상 회원의 로그인 ID
     * @param memberId null이 아닌 대상 회원 ID
     * @param logType  null이 아닌 활동 로그 유형
     * @param details  null이 아닌 활동 상세 내용 (빈 문자열 허용)
     */
    void publishMemberActivity(final String loginId, final Long memberId, final LogType logType, final String details);
}
