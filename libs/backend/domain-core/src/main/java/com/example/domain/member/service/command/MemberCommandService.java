package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberRoleUpdateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;

import java.util.List;

public interface MemberCommandService {

    /**
     * 전략 패턴(Factory)에서 역할별 구현체를 자동 등록하기 위해, 서비스가 지원하는 권한 목록을 반환합니다.
     *
     * <p>
     * [CQRS 경계 예외]
     * 이 메서드는 Query 성격(데이터 반환, 부작용 없음)이지만 CommandService에 정의됩니다.
     * 이유: 서비스 레지스트리에서 역할별 구현체를 자동 등록하기 위한 내부 메타데이터 조회 용도이며,
     * 비즈니스 데이터 조회가 아닌 설정/라우팅 목적이므로 CQRS 위반으로 보지 않습니다.
     * </p>
     *
     * <p>
     * [주의]
     * - AOP 프록시(JDK Dynamic Proxy) 환경에서도 안전하게 동작하도록
     * "인터페이스"에 메서드를 정의합니다.
     * </p>
     */
    List<AccountRole> getSupportedRoles();

    /**
     * 새 회원을 생성합니다.
     *
     * @param command 로그인 ID, 닉네임, 비밀번호, 권한, 회원 유형을 포함하는 명령 (null 불가)
     * @return 생성된 회원의 ID (null이 아닌 양수 보장)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    Long createMember(final MemberCreateCommand command);

    /**
     * 기존 회원 정보를 수정합니다.
     *
     * @param command 닉네임, 비밀번호, 대상 회원 ID를 포함하는 명령 (null 불가)
     * @return 수정된 회원의 ID (null이 아닌 양수 보장)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    Long updateMember(final MemberUpdateCommand command);

    /**
     * 회원을 비활성화(탈퇴) 처리합니다.
     *
     * @param command 대상 회원 ID, 현재 계정 ID, 로그아웃 명령을 포함하는 명령 (null 불가)
     * @return 비활성화된 회원의 ID (null이 아닌 양수 보장)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    Long deactivateMember(final MemberDeactivateCommand command);

    /**
     * 회원의 권한(Role)을 변경합니다.
     *
     * @param command 대상 회원 ID와 변경할 권한을 포함하는 명령 (null 불가)
     * @throws IllegalArgumentException command가 null이거나 필수 필드가 누락된 경우
     */
    void updateMemberRole(final MemberRoleUpdateCommand command);

}
