package com.example.domain.aws.payload.dto;

public record AdminS3ImageDeleteCommand(
        Long memberImageId
) {

    public static AdminS3ImageDeleteCommand of(final Long memberImageId) {
        return new AdminS3ImageDeleteCommand(memberImageId);
    }
}
