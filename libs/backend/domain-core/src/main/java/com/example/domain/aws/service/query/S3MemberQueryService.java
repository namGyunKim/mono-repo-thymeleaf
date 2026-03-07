package com.example.domain.aws.service.query;

import com.example.domain.aws.service.common.S3BucketResolver;
import com.example.domain.aws.service.common.S3ImageUrlSupport;
import com.example.domain.aws.service.common.S3KeyBuilder;
import com.example.domain.aws.service.common.S3UrlParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@Transactional(readOnly = true)
public class S3MemberQueryService extends AbstractS3QueryService {

    public S3MemberQueryService(final S3Client s3Client, final S3BucketResolver s3BucketResolver, final S3KeyBuilder s3KeyBuilder, final S3UrlParser s3UrlParser, final S3ImageUrlSupport s3ImageUrlSupport) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser, s3ImageUrlSupport);
    }
}
