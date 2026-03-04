package com.example.domain.member.repository;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {

    Optional<Member> findByLoginId(final String loginId);

    Optional<Member> findByLoginIdAndRole(final String loginId, final AccountRole role);

    Optional<LoginMemberViewProjection> findProjectedByLoginIdAndRole(final String loginId, final AccountRole role);

    boolean existsByLoginId(final String loginId);

    boolean existsByNickName(final String nickName);

    boolean existsByNickNameAndLoginIdNot(final String nickName, final String excludedLoginId);

    boolean existsByRole(final AccountRole role);

    Optional<Member> findByIdAndRoleIn(final Long id, final List<AccountRole> roles);

    Optional<Member> findByLoginIdAndRoleIn(final String loginId, final List<AccountRole> roles);
}
