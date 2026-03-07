package com.example.domain.aws.service.command;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.S3ImageDeleteCommand;
import com.example.domain.aws.service.common.*;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@Transactional
public class S3MemberCommandService extends AbstractS3CommandService {

    public S3MemberCommandService(final S3Client s3Client, final S3BucketResolver s3BucketResolver, final S3KeyBuilder s3KeyBuilder, final S3UrlParser s3UrlParser, final S3UploadSupport s3UploadSupport, final S3DeleteSupport s3DeleteSupport, final S3CloneSupport s3CloneSupport) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser, s3UploadSupport, s3DeleteSupport, s3CloneSupport);
    }

    @Override
    protected void validateImageType(final ImageType imageType) {
        ImageType.validateMemberUploadType(imageType);
    }

    @Override
    public void deleteImages(final S3ImageDeleteCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 삭제 요청 값은 필수입니다.");
        }
        super.deleteImages(command);
    }

}
