package com.example.domain.member.service.query;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberOrderType;
import com.example.domain.member.payload.dto.MemberDetailQuery;
import com.example.domain.member.payload.dto.MemberListQuery;
import com.example.domain.member.payload.dto.MemberRoleExistsQuery;
import com.example.domain.member.payload.response.MemberDetailResponse;
import com.example.domain.member.payload.response.MemberListResponse;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.member.repository.MemberSpecification;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.GlobalException;
import com.example.global.utils.PaginationUtils;
import com.querydsl.core.types.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 QueryDSL + Pageable 조회 로직을 한 곳으로 모은 추상 클래스입니다.
 * <p>
 * - 역할(Role)별 서비스 분리는 유지하되, 중복 코드를 줄여 유지보수 비용을 낮춥니다.
 * - USER 조회 시에만 비활성화(INACTIVE) 계정 접근을 차단하는 훅을 제공합니다.
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
public abstract class AbstractQueryDslMemberQueryService extends AbstractMemberQueryService {

    protected final MemberRepository memberRepository;

    @Override
    public boolean existsByRole(final MemberRoleExistsQuery query) {
        if (query == null || query.role() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "role은 필수입니다.");
        }
        return memberRepository.existsByRole(query.role());
    }

    @Override
    public Page<MemberListResponse> getList(final MemberListQuery request) {
        if (request == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "요청 값이 비어있습니다.");
        }

        final Sort sort = (request.order() == MemberOrderType.CREATE_ASC)
                ? Sort.by(Sort.Direction.ASC, "createdAt")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        final Pageable pageable = PaginationUtils.toPageable(
                request.page(),
                request.size(),
                PaginationUtils.DEFAULT_SIZE,
                sort
        );

        final List<AccountRole> roles = getSupportedRoles();
        final Predicate predicate = MemberSpecification.searchMember(request, roles);
        return memberRepository.findAll(predicate, pageable).map(MemberListResponse::from);
    }

    @Override
    public MemberDetailResponse getDetail(final MemberDetailQuery query) {
        if (query == null || query.id() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 식별자는 필수입니다.");
        }

        final Member member = memberRepository.findByIdAndRoleIn(query.id(), getSupportedRoles())
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));

        return MemberDetailResponse.from(member);
    }
}
