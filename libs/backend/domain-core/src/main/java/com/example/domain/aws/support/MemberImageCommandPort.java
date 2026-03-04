package com.example.domain.aws.support;

/**
 * AWS 도메인이 회원 이미지 등록/삭제를 요청하는 Port
 *
 * <p>
 * - Port 정의: aws/support (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * - primitive 파라미터로 도메인 간 결합도를 최소화
 * </p>
 */
public interface MemberImageCommandPort {

    /**
     * 회원 프로필 이미지를 등록하고 생성된 이미지 ID를 반환한다.
     *
     * @param memberId     null이 아닌 이미지를 등록할 회원 ID
     * @param uploadDirect null이 아닌 비어 있지 않은 업로드 디렉토리 경로
     * @param fileName     null이 아닌 비어 있지 않은 이미지 파일명
     * @return 생성된 회원 이미지 ID (null이 아님)
     * @throws com.example.global.exception.GlobalException 회원이 존재하지 않을 경우
     */
    Long registerProfileImage(final Long memberId, final String uploadDirect, final String fileName);

    /**
     * 회원 프로필 이미지를 삭제한다.
     *
     * @param memberImageId null이 아닌 삭제할 회원 이미지 ID
     * @throws com.example.global.exception.GlobalException 이미지가 존재하지 않을 경우
     */
    void deleteProfileImage(final Long memberImageId);
}
