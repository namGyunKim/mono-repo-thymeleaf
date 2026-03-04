package com.example.domain.member.support;

import com.example.domain.member.entity.Member;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.security.port.SecurityMemberTokenInfo;
import com.example.domain.security.port.SecurityMemberTokenPort;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * SecurityMemberTokenPort 어댑터 — Member 도메인이 제공
 */
@Component
@RequiredArgsConstructor
public class SecurityMemberTokenPortAdapter implements SecurityMemberTokenPort {

    private final MemberRepository memberRepository;

    @Override
    public Optional<SecurityMemberTokenInfo> findTokenInfoById(final Long memberId) {
        if (memberId == null || memberId <= 0) {
            return Optional.empty();
        }
        return memberRepository.findById(memberId)
                .map(this::toTokenInfo);
    }

    @Override
    public Optional<SecurityMemberTokenInfo> findTokenInfoByLoginId(final String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return Optional.empty();
        }
        return memberRepository.findByLoginId(loginId)
                .map(this::toTokenInfo);
    }

    @Override
    public void updateRefreshTokenEncrypted(final Long memberId, final String encrypted) {
        if (memberId == null || memberId <= 0) {
            return;
        }
        memberRepository.findById(memberId)
                .ifPresent(member -> member.updateRefreshTokenEncrypted(encrypted));
    }

    @Override
    public void revokeTokens(final Long memberId) {
        if (memberId == null || memberId <= 0) {
            return;
        }
        memberRepository.findById(memberId)
                .ifPresent(member -> {
                    member.rotateTokenVersion();
                    member.invalidateRefreshTokenEncrypted();
                });
    }

    private SecurityMemberTokenInfo toTokenInfo(final Member member) {
        return SecurityMemberTokenInfo.of(
                member.getId(),
                member.getLoginId(),
                member.getRole(),
                member.getNickName(),
                member.getMemberType(),
                member.getActive(),
                member.getTokenVersion(),
                member.getRefreshTokenEncrypted()
        );
    }
}
