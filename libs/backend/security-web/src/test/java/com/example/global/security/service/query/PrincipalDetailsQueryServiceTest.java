package com.example.global.security.service.query;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;
import com.example.domain.security.guard.PrincipalDetails;
import com.example.domain.security.port.SecurityAccountAuthQueryPort;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrincipalDetailsQueryServiceTest {

    @Mock
    private SecurityAccountAuthQueryPort securityAccountAuthQueryPort;

    @InjectMocks
    private PrincipalDetailsQueryService principalDetailsQueryService;

    @Test
    @DisplayName("loadUserByUsername - 정상 일반 사용자 조회 시 PrincipalDetails 반환")
    void loadUserByUsername_existingGeneralUser_returnsPrincipalDetails() {
        // Arrange
        final String loginId = "user01";
        final AccountAuthMemberView member = AccountAuthMemberView.of(
                1L, loginId, "encodedPassword", "닉네임",
                AccountRole.USER, MemberType.GENERAL, MemberActiveStatus.ACTIVE
        );
        when(securityAccountAuthQueryPort.findActiveMemberForAuthByLoginId(any(AccountLoginIdQuery.class)))
                .thenReturn(member);

        // Act
        final var result = principalDetailsQueryService.loadUserByUsername(loginId);

        // Assert
        assertThat(result).isInstanceOf(PrincipalDetails.class);
        final PrincipalDetails principalDetails = (PrincipalDetails) result;
        assertThat(principalDetails.getUsername()).isEqualTo(loginId);
        assertThat(principalDetails.getId()).isEqualTo(1L);
        assertThat(principalDetails.getRole()).isEqualTo(AccountRole.USER);
        assertThat(principalDetails.getMemberType()).isEqualTo(MemberType.GENERAL);
        verify(securityAccountAuthQueryPort).findActiveMemberForAuthByLoginId(any(AccountLoginIdQuery.class));
    }

    @Test
    @DisplayName("loadUserByUsername - GENERAL이 아닌 MemberType 조회 시 UsernameNotFoundException 발생")
    void loadUserByUsername_nonGeneralMemberType_throwsUsernameNotFoundException() {
        // Arrange
        final String loginId = "googleUser";
        final AccountAuthMemberView member = AccountAuthMemberView.of(
                2L, loginId, "password", "구글유저",
                AccountRole.USER, MemberType.GOOGLE, MemberActiveStatus.ACTIVE
        );
        when(securityAccountAuthQueryPort.findActiveMemberForAuthByLoginId(any(AccountLoginIdQuery.class)))
                .thenReturn(member);

        // Act & Assert
        assertThatThrownBy(() -> principalDetailsQueryService.loadUserByUsername(loginId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(loginId);
    }

    @Test
    @DisplayName("loadUserByUsername - 비활성 계정 조회 시 DisabledException 발생")
    void loadUserByUsername_inactiveMember_throwsDisabledException() {
        // Arrange
        final String loginId = "inactiveUser";
        when(securityAccountAuthQueryPort.findActiveMemberForAuthByLoginId(any(AccountLoginIdQuery.class)))
                .thenThrow(new GlobalException(ErrorCode.MEMBER_INACTIVE));

        // Act & Assert
        assertThatThrownBy(() -> principalDetailsQueryService.loadUserByUsername(loginId))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining(loginId);
    }

    @Test
    @DisplayName("loadUserByUsername - 존재하지 않는 사용자 조회 시 UsernameNotFoundException 발생")
    void loadUserByUsername_nonExistingUser_throwsUsernameNotFoundException() {
        // Arrange
        final String loginId = "unknownUser";
        when(securityAccountAuthQueryPort.findActiveMemberForAuthByLoginId(any(AccountLoginIdQuery.class)))
                .thenThrow(new GlobalException(ErrorCode.MEMBER_NOT_EXIST));

        // Act & Assert
        assertThatThrownBy(() -> principalDetailsQueryService.loadUserByUsername(loginId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(loginId);
    }

    @Test
    @DisplayName("loadUserByUsername - 기타 GlobalException 발생 시 InternalAuthenticationServiceException 발생")
    void loadUserByUsername_unexpectedGlobalException_throwsInternalAuthenticationServiceException() {
        // Arrange
        final String loginId = "errorUser";
        when(securityAccountAuthQueryPort.findActiveMemberForAuthByLoginId(any(AccountLoginIdQuery.class)))
                .thenThrow(new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR));

        // Act & Assert
        assertThatThrownBy(() -> principalDetailsQueryService.loadUserByUsername(loginId))
                .isInstanceOf(InternalAuthenticationServiceException.class)
                .hasMessageContaining("인증 처리 중 오류가 발생했습니다.");
    }
}
