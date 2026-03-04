package com.example.domain.member.support;

import com.example.domain.member.payload.dto.MemberImagesStorageDeleteCommand;
import com.example.domain.member.payload.dto.MemberImageStorageDeleteCommand;

/**
 * member 도메인에서 외부 이미지 저장소(S3 등)의 회원 이미지를 관리하는 포트.
 *
 * <p>방향: member → aws(인프라)
 *
 * <p>MemberImageCommandService / AbstractMemberCommandService가
 * S3MemberCommandService에 직접 의존하지 않도록 추상화한다.
 */
public interface MemberImageStoragePort {

    /**
     * 회원 이미지 한 건을 저장소에서 삭제한다.
     *
     * @param command null이 아닌 단건 이미지 삭제 커맨드.
     *                memberId, fileName, uploadDirect가 모두 필수이다.
     * @throws com.example.global.exception.GlobalException 필수 필드가 누락된 경우
     */
    void deleteImage(final MemberImageStorageDeleteCommand command);

    /**
     * 회원 이미지 여러 건을 저장소에서 일괄 삭제한다.
     *
     * @param command null이 아닌 다건 이미지 삭제 커맨드.
     *                memberId, uploadDirect, fileNames(비어 있지 않은 리스트)가 모두 필수이다.
     * @throws com.example.global.exception.GlobalException 필수 필드가 누락된 경우
     */
    void deleteImages(final MemberImagesStorageDeleteCommand command);
}
