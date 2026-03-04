package com.example.domain.aws.service.common;

import com.example.domain.aws.payload.dto.S3ImageUrlQuery;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.GlobalException;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.s3.S3Client;

@Component
public class S3ImageUrlSupport extends AbstractS3ServiceSupport {

    public S3ImageUrlSupport(final S3Client s3Client, final S3BucketResolver s3BucketResolver, final S3KeyBuilder s3KeyBuilder, final S3UrlParser s3UrlParser) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
    }

    public String resolve(final S3ImageUrlQuery query) {
        if (query == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 URL 조회 요청 값이 비어있습니다.");
        }
        return buildPublicImageUrl(query.fileName(), query.imageType(), query.entityId());
    }
}
