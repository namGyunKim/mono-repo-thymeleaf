package com.example.domain.member.service.query;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.dto.MemberDetailQuery;
import com.example.domain.member.payload.dto.MemberListQuery;
import com.example.domain.member.payload.dto.MemberRoleExistsQuery;
import com.example.domain.member.payload.response.MemberDetailResponse;
import com.example.domain.member.payload.response.MemberListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MemberQueryService {

    /**
     * 전략 패턴(Factory)에서 역할별 구현체를 자동 등록하기 위해, 서비스가 지원하는 권한 목록을 반환합니다.
     *
     * <p>
     * [주의]
     * - AOP 프록시(JDK Dynamic Proxy) 환경에서도 안전하게 동작하도록
     * "인터페이스"에 메서드를 정의합니다.
     * </p>
     */
    List<AccountRole> getSupportedRoles();

    /**
     * 해당 권한(Role)을 가진 회원이 존재하는지 확인합니다.
     *
     * @param query 확인할 권한 정보를 포함하는 조회 조건 (null 불가)
     * @return 해당 권한을 가진 회원이 존재하면 {@code true}
     * @throws IllegalArgumentException query가 null이거나 권한 값이 누락된 경우
     */
    boolean existsByRole(final MemberRoleExistsQuery query);

    /**
     * 회원 목록을 페이징 조건에 맞춰 조회합니다.
     *
     * @param memberUserListQuery 페이지, 정렬, 검색어, 필터, 활성 상태를 포함하는 조회 조건 (null 불가)
     * @return 페이징된 회원 목록 응답 (null이 아닌 Page 보장)
     * @throws IllegalArgumentException query가 null인 경우
     */
    Page<MemberListResponse> getList(final MemberListQuery memberUserListQuery);

    /**
     * 회원 상세 정보를 조회합니다.
     *
     * @param query 대상 회원 ID를 포함하는 조회 조건 (null 불가)
     * @return 회원 상세 응답 (null이 아닌 값 보장)
     * @throws IllegalArgumentException query가 null이거나 ID가 누락된 경우
     */
    MemberDetailResponse getDetail(final MemberDetailQuery query);
}
