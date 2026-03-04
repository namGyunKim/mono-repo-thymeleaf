package com.example.domain.social.support;

import com.example.domain.member.entity.Member;

/**
 * social 도메인에서 member 도메인의 회원 등록/조회 기능을 사용하는 포트.
 *
 * <p>방향: social → member
 *
 * <p>Social 도메인은 MemberRepository를 직접 참조하지 않는다.
 * Port 정의는 social/support(사용 도메인)에, Adapter 구현은 member/support(제공 도메인)에 배치한다.
 * 반환 타입이 Member 엔티티인 이유는 SocialAccount 엔티티의 {@code @ManyToOne} JPA 연관관계에 필요하기 때문이다.
 */
public interface SocialMemberRegistrationPort {

    /**
     * 소셜 회원을 저장한다.
     *
     * <p>신규 소셜 로그인 시 회원 엔티티를 영속화하고,
     * SocialAccount와의 JPA 연관관계 매핑을 위해 저장된 엔티티를 반환한다.
     *
     * @param member null이 아닌 저장할 회원 엔티티
     * @return 저장된 Member 엔티티 (ID가 할당된 상태, null이 아님)
     */
    Member saveSocialMember(final Member member);

    /**
     * 닉네임의 존재 여부를 확인한다.
     *
     * <p>소셜 회원 등록 시 닉네임 중복 검사에 사용한다.
     *
     * @param nickName null이 아닌 비어 있지 않은 확인할 닉네임
     * @return 해당 닉네임이 이미 존재하면 {@code true}
     */
    boolean existsByNickName(final String nickName);
}
