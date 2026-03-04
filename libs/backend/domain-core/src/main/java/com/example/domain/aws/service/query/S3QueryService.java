package com.example.domain.aws.service.query;

import com.example.domain.aws.payload.dto.S3ImageUrlQuery;

public interface S3QueryService {

    /**
     * S3에 저장된 이미지의 접근 URL을 조회합니다.
     *
     * @param query 파일명, 이미지 타입, 엔티티 ID를 포함하는 조회 조건 (null 불가)
     * @return 이미지 접근 URL 문자열 (null이 아닌 값 보장)
     * @throws IllegalArgumentException query가 null이거나 필수 필드가 누락된 경우
     */
    String getImageUrl(final S3ImageUrlQuery query);
}
