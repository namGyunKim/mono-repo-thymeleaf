package com.example.domain.social.payload.dto;

import com.example.domain.social.enums.SocialProvider;

public record SocialAccountKeyQuery(
        SocialProvider provider,
        String socialKey
) {

    public static SocialAccountKeyQuery of(final SocialProvider provider, final String socialKey) {
        return new SocialAccountKeyQuery(provider, socialKey);
    }
}
