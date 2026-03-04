package com.example.domain.account.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountProfileUpdateCommand;

/**
 * account 도메인에서 member 도메인의 회원 프로필을 변경하는 커맨드 포트.
 *
 * <p>방향: account → member
 *
 * <p>AccountCommandService가 MemberStrategyFactory / MemberCommandService에
 * 직접 의존하지 않도록 추상화한다.
 */
public interface AccountMemberCommandPort {

    /**
     * 회원 프로필(닉네임, 비밀번호 등)을 수정한다.
     *
     * @param command null이 아닌 프로필 수정 커맨드.
     *                내부에 currentAccount, nickName이 필수로 포함되어야 한다.
     * @return 수정된 회원의 ID (null이 아님)
     */
    Long updateMemberProfile(final AccountProfileUpdateCommand command);

    /**
     * 회원을 비활성화(탈퇴) 처리한다.
     *
     * @param role     null이 아닌 대상 회원의 역할
     * @param memberId null이 아닌 대상 회원 ID
     */
    void deactivateMember(final AccountRole role, final Long memberId);
}
