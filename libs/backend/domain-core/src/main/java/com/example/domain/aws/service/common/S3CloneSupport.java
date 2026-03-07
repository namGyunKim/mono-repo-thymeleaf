package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.S3UrlParts;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MetadataDirective;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class S3CloneSupport extends AbstractS3ServiceSupport {

    public S3CloneSupport(final S3Client s3Client, final S3BucketResolver s3BucketResolver, final S3KeyBuilder s3KeyBuilder, final S3UrlParser s3UrlParser) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
    }

    public String cloneFromUrl(final String sourceS3Url, final ImageType destinationImageType, final Long destinationEntityId) {
        final long startNanos = System.nanoTime();
        logCloneStart(sourceS3Url, destinationImageType, destinationEntityId);
        final S3UrlParts source = parseS3Url(sourceS3Url);
        final String originalFilename = fetchOriginalFilename(source, sourceS3Url);
        final String finalFileName = generateFinalUploadFileName(destinationImageType, originalFilename);
        final String destinationKey = generateS3Key(finalFileName, destinationImageType, destinationEntityId);

        try {
            final CopyObjectRequest copyRequest = buildCopyRequest(source, destinationKey, originalFilename);
            executeCopy(copyRequest);
            logCloneSuccess(source, destinationKey, destinationImageType, destinationEntityId, elapsedMillis(startNanos));
        } catch (final Exception e) {
            logCloneFailure(sourceS3Url, source, destinationImageType, destinationEntityId, destinationKey, elapsedMillis(startNanos), e);
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED, "S3 간 복사에 실패했습니다.");
        }

        return finalFileName;
    }

    private void logCloneStart(final String sourceS3Url, final ImageType destinationImageType, final Long destinationEntityId) {
        log.info(
                "S3-to-S3 복사 시작: sourceUrl={}, destType={}, destEntityId={}",
                sourceS3Url,
                destinationImageType.name(),
                destinationEntityId
        );
    }

    private void logCloneSuccess(final S3UrlParts source, final String destinationKey, final ImageType destinationImageType, final Long destinationEntityId, final long elapsedMs) {
        log.info(
                """
                        resultCode=SUCCESS, S3-to-S3 복사 완료: sourceBucket={}, sourceKey={}, destinationBucket={}, destinationKey={}, destinationImageType={}, destinationEntityId={}, elapsedMs={}
                        """.stripTrailing(),
                source.bucketName(),
                source.objectKey(),
                bucketName,
                destinationKey,
                destinationImageType.name(),
                destinationEntityId,
                elapsedMs
        );
    }

    private void logCloneFailure(final String sourceS3Url, final S3UrlParts source, final ImageType destinationImageType, final Long destinationEntityId, final String destinationKey, final long elapsedMs, final Exception e) {
        final String sourceBucket = source != null ? source.bucketName() : "UNKNOWN";
        final String sourceKey = source != null ? source.objectKey() : "UNKNOWN";
        log.error(
                """
                        errorCode={}, exceptionName={}, resultCode=FAILED, S3-to-S3 복사 실패: sourceUrl={}, sourceBucket={}, sourceKey={}, destinationBucket={}, destinationKey={}, destinationImageType={}, destinationEntityId={}, elapsedMs={}, errorMessage={}
                        """.stripTrailing(),
                ErrorCode.FILE_UPLOAD_FAILED.getCode(),
                e.getClass().getSimpleName(),
                sourceS3Url,
                sourceBucket,
                sourceKey,
                bucketName,
                destinationKey,
                destinationImageType.name(),
                destinationEntityId,
                elapsedMs,
                e.getMessage(),
                e
        );
    }

    private long elapsedMillis(final long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private String fetchOriginalFilename(final S3UrlParts source, final String sourceS3Url) {
        try {
            final HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(source.bucketName())
                    .key(source.objectKey())
                    .build();
            final HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            return extractFilenameFromContentDisposition(headResponse.contentDisposition());
        } catch (final Exception e) {
            log.warn(
                    """
                            errorCode={}, exceptionName={}, S3 원본 파일 조회 실패: bucket={}, key={}, sourceUrl={}, errorMessage={}
                            """.stripTrailing(),
                    ErrorCode.FILE_NOT_FOUND.getCode(),
                    e.getClass().getSimpleName(),
                    source.bucketName(),
                    source.objectKey(),
                    sourceS3Url,
                    e.getMessage(),
                    e
            );
            throw new GlobalException(ErrorCode.FILE_NOT_FOUND, "원본 S3 파일을 찾을 수 없거나 접근할 수 없습니다.");
        }
    }

    private CopyObjectRequest buildCopyRequest(final S3UrlParts source, final String destinationKey, final String originalFilename) {
        return CopyObjectRequest.builder()
                .sourceBucket(source.bucketName())
                .sourceKey(source.objectKey())
                .destinationBucket(bucketName)
                .destinationKey(destinationKey)
                .contentDisposition(buildContentDisposition(originalFilename))
                .metadataDirective(MetadataDirective.REPLACE)
                .build();
    }

    private void executeCopy(final CopyObjectRequest copyRequest) {
        s3Client.copyObject(copyRequest);
    }

}
