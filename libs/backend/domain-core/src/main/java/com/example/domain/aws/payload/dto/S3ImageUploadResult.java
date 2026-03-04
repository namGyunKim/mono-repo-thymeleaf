package com.example.domain.aws.payload.dto;

public record S3ImageUploadResult(
        Long memberImageId,
        String fileName,
        String imageUrl
) {

    public static S3ImageUploadResult of(final Long memberImageId, final String fileName, final String imageUrl) {
        return new S3ImageUploadResult(memberImageId, fileName, imageUrl);
    }
}
