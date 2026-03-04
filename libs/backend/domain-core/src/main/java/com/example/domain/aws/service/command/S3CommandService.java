package com.example.domain.aws.service.command;

import com.example.domain.aws.payload.dto.S3ImageCloneCommand;
import com.example.domain.aws.payload.dto.S3ImageDeleteCommand;
import com.example.domain.aws.payload.dto.S3ImagesCloneCommand;
import com.example.domain.aws.payload.dto.S3ImagesUploadCommand;
import com.example.domain.aws.payload.dto.S3ImageUploadCommand;
import com.example.domain.aws.payload.dto.S3SingleImageDeleteCommand;

import java.util.List;

public interface S3CommandService {

    /**
     * 단일 이미지를 S3에 업로드합니다.
     *
     * @param command 업로드 대상 파일, 이미지 타입, 엔티티 ID를 포함하는 명령 (null 불가)
     * @return 업로드된 이미지의 파일명 (null이 아닌 값 보장)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    String uploadImage(final S3ImageUploadCommand command);

    /**
     * 여러 이미지를 S3에 일괄 업로드합니다.
     *
     * @param command 업로드 대상 파일 목록, 이미지 타입, 엔티티 ID를 포함하는 명령 (null 불가)
     * @return 업로드된 이미지의 파일명 목록 (빈 목록 가능, null 아님)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    List<String> uploadImages(final S3ImagesUploadCommand command);

    /**
     * S3에서 단일 이미지를 삭제합니다.
     *
     * @param command 삭제 대상 파일명, 이미지 타입, 엔티티 ID를 포함하는 명령 (null 불가)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    void deleteImage(final S3SingleImageDeleteCommand command);

    /**
     * S3에서 여러 이미지를 일괄 삭제합니다.
     *
     * @param command 삭제 대상 파일명 목록, 이미지 타입, 엔티티 ID를 포함하는 명령 (null 불가)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    void deleteImages(final S3ImageDeleteCommand command);

    /**
     * S3 내의 한 객체(이미지)를 다른 경로로 복사합니다. (S3-to-S3 copy)
     *
     * @param command 복사 요청 커맨드
     * @return 복사된 이미지의 파일명
     */
    String cloneImageFromUrl(final S3ImageCloneCommand command);

    /**
     * S3 내의 여러 객체(이미지)를 다른 경로로 복사합니다.
     *
     * @param command 복사 요청 커맨드
     * @return 복사된 이미지의 파일명 목록
     */
    List<String> cloneImagesFromUrls(final S3ImagesCloneCommand command);
}
