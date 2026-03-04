package com.example.domain.social.google.support;

import com.example.domain.member.entity.Member;
import com.example.domain.social.entity.SocialAccount;
import com.example.domain.social.enums.SocialProvider;
import com.example.domain.social.payload.dto.SocialAccountKeyQuery;
import com.example.domain.social.payload.dto.SocialAccountMemberProviderQuery;
import com.example.domain.social.repository.SocialAccountRepository;
import com.example.global.security.SocialTokenCrypto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleSocialAccountManager {

    private final SocialAccountRepository socialAccountRepository;
    private final SocialTokenCrypto socialTokenCrypto;

    public Optional<SocialAccount> findBySocialKey(final String socialKey) {
        return socialAccountRepository.findByProviderAndSocialKey(
                SocialAccountKeyQuery.of(SocialProvider.GOOGLE, socialKey)
        );
    }

    public Optional<SocialAccount> findByMemberId(final Long memberId) {
        return socialAccountRepository.findByMemberIdAndProvider(
                SocialAccountMemberProviderQuery.of(memberId, SocialProvider.GOOGLE)
        );
    }

    public SocialAccount create(final Member member, final String socialKey) {
        return SocialAccount.from(member, SocialProvider.GOOGLE, socialKey);
    }

    public void save(final SocialAccount socialAccount) {
        socialAccountRepository.save(socialAccount);
    }

    public void deleteIfExists(final SocialAccount socialAccount) {
        if (socialAccount == null) {
            return;
        }
        socialAccountRepository.delete(socialAccount);
    }

    public void updateRefreshTokenIfPresent(final SocialAccount socialAccount, final String refreshToken) {
        if (socialAccount == null || !StringUtils.hasText(refreshToken)) {
            return;
        }
        final String encrypted = socialTokenCrypto.encrypt(refreshToken);
        socialAccount.updateRefreshTokenEncrypted(encrypted);
    }
}
