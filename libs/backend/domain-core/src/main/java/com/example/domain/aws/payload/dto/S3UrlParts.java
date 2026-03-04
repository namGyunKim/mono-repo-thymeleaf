package com.example.domain.aws.payload.dto;

/**
 * S3 URL 파싱 결과 DTO
 */
public record S3UrlParts(
        String bucketName,
        String objectKey
) {

    public static S3UrlParts of(final String bucketName, final String objectKey) {
        return new S3UrlParts(bucketName, objectKey);
    }
}
