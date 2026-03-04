package com.example.domain.member.api;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberDetailQuery;
import com.example.domain.member.payload.dto.MemberListQuery;
import com.example.domain.member.payload.dto.MemberRoleUpdateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.payload.request.MemberCreateRequest;
import com.example.domain.member.payload.request.MemberListRequest;
import com.example.domain.member.payload.request.MemberRoleUpdateRequest;
import com.example.domain.member.payload.request.MemberUpdateRequest;
import com.example.domain.member.payload.response.MemberDetailResponse;
import com.example.domain.member.payload.response.MemberListResponse;
import com.example.domain.member.service.command.MemberCommandService;
import com.example.domain.member.service.MemberStrategyFactory;
import com.example.domain.member.service.query.MemberQueryService;
import com.example.domain.member.validator.MemberCreateRequestPolicyValidator;
import com.example.domain.member.validator.MemberCreateValidator;
import com.example.domain.member.validator.MemberListRequestPolicyValidator;
import com.example.global.annotation.CurrentAccount;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.IdResponse;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.version.ApiVersioning;


import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ConditionalOnProperty(name = "app.type", havingValue = "admin")
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("@memberGuard.hasAnyAdminRole()")
public class AdminMemberApiController {

    private final MemberStrategyFactory memberStrategyFactory;

    private final MemberCreateValidator memberCreateValidator;
    private final MemberCreateRequestPolicyValidator memberCreateRequestPolicyValidator;
    private final MemberListRequestPolicyValidator memberListRequestPolicyValidator;

    private final RestApiController restApiController;

    @InitBinder
    public void initMemberCreateRequestBinder(final WebDataBinder binder) {
        // @RequestBody 요청은 objectName 의존을 줄이기 위해 무인자 @InitBinder로 등록합니다.
        // 실제 적용 대상은 각 Validator의 supports(...) 타입 체크로 제한합니다.
        binder.addValidators(memberCreateValidator, memberCreateRequestPolicyValidator);
    }

    @InitBinder("memberListRequest")
    public void initMemberListRequestBinder(final WebDataBinder binder) {
        binder.addValidators(memberListRequestPolicyValidator);
    }

    @PostMapping(version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canManageRole(#memberCreateRequest.toDomainRole())")
    public ResponseEntity<RestApiResponse<IdResponse>> createMember(@Valid @RequestBody final MemberCreateRequest memberCreateRequest) {
        final MemberCommandService service = memberStrategyFactory.getCommandService(memberCreateRequest.toDomainRole());
        final Long createdId = service.createMember(MemberCreateCommand.from(memberCreateRequest));
        final IdResponse createResponse = IdResponse.of(createdId);

        return restApiController.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(createResponse.id())
                        .toUri(),
                createResponse
        );
    }

    @GetMapping(version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canManageRole(#memberListRequest.toDomainRole())")
    public ResponseEntity<RestApiResponse<Page<MemberListResponse>>> getMemberList(@Valid @ModelAttribute("memberListRequest") final MemberListRequest memberListRequest) {
        final MemberQueryService service = memberStrategyFactory.getQueryService(memberListRequest.toDomainRole());
        final Page<MemberListResponse> memberPage = service.getList(MemberListQuery.from(memberListRequest));

        return restApiController.ok(memberPage);
    }

    @GetMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canAccessMember(#id)")
    public ResponseEntity<RestApiResponse<MemberDetailResponse>> getMemberDetail(@PathVariable final Long id) {
        final MemberQueryService service = memberStrategyFactory.getQueryServiceByMemberId(id);
        final MemberDetailResponse response = service.getDetail(MemberDetailQuery.of(id));

        return restApiController.ok(response);
    }

    @PutMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canAccessMember(#id)")
    public ResponseEntity<RestApiResponse<IdResponse>> updateMember(@PathVariable final Long id, @Valid @RequestBody final MemberUpdateRequest memberUpdateRequest) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandServiceByMemberId(id);
        final Long updatedId = commandService.updateMember(
                MemberUpdateCommand.from(memberUpdateRequest, id)
        );
        final IdResponse updateResponse = IdResponse.of(updatedId);

        return restApiController.ok(updateResponse);
    }

    @DeleteMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canAccessMember(#id)")
    public ResponseEntity<Void> deactivateMember(@PathVariable final Long id, @CurrentAccount final CurrentAccountDTO currentAccount) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandServiceByMemberId(id);
        final MemberDeactivateCommand deactivateCommand = MemberDeactivateCommand.of(id, currentAccount.id());
        commandService.deactivateMember(deactivateCommand);
        return restApiController.noContent();
    }

    @PatchMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isSuperAdmin()")
    public ResponseEntity<RestApiResponse<IdResponse>> updateMemberRole(@PathVariable final Long id, @Valid @RequestBody final MemberRoleUpdateRequest memberRoleUpdateRequest) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandServiceByMemberId(id);
        commandService.updateMemberRole(MemberRoleUpdateCommand.from(id, memberRoleUpdateRequest));

        return restApiController.ok(IdResponse.of(id));
    }

}
