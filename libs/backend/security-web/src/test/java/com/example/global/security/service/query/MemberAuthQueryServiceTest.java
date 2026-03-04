package com.example.global.security.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberLoginIdQuery;
import com.example.domain.security.guard.support.SecurityMemberAccessPort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MemberAuthQueryServiceTest {

    @Mock
    private SecurityMemberAccessPort securityMemberAccessPort;

    @InjectMocks
    private MemberAuthQueryService memberAuthQueryService;

    // === findActiveMemberForAuthentication ===

    @Test
    @DisplayName("findActiveMemberForAuthentication - 정상 loginId로 조회 시 회원 정보 반환")
    void findActiveMemberForAuthentication_validLoginId_returnsMember() {
        // Arrange
        final String loginId = "user01";
        final MemberLoginIdQuery query = MemberLoginIdQuery.of(loginId);
        final AccountAuthMemberView expectedMember = AccountAuthMemberView.of(
                1L, loginId, "encodedPassword", "닉네임",
                AccountRole.USER, MemberType.GENERAL, MemberActiveStatus.ACTIVE, 1L
        );
        when(securityMemberAccessPort.findActiveAuthMemberByLoginId(loginId))
                .thenReturn(Optional.of(expectedMember));

        // Act
        final Optional<AccountAuthMemberView> result = memberAuthQueryService.findActiveMemberForAuthentication(query);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().loginId()).isEqualTo(loginId);
        verify(securityMemberAccessPort).findActiveAuthMemberByLoginId(loginId);
    }

    @Test
    @DisplayName("findActiveMemberForAuthentication - 존재하지 않는 loginId 조회 시 empty 반환")
    void findActiveMemberForAuthentication_nonExistingLoginId_returnsEmpty() {
        // Arrange
        final String loginId = "unknownUser";
        final MemberLoginIdQuery query = MemberLoginIdQuery.of(loginId);
        when(securityMemberAccessPort.findActiveAuthMemberByLoginId(loginId))
                .thenReturn(Optional.empty());

        // Act
        final Optional<AccountAuthMemberView> result = memberAuthQueryService.findActiveMemberForAuthentication(query);

        // Assert
        assertThat(result).isEmpty();
        verify(securityMemberAccessPort).findActiveAuthMemberByLoginId(loginId);
    }

    @Test
    @DisplayName("findActiveMemberForAuthentication - null query 전달 시 empty 반환")
    void findActiveMemberForAuthentication_nullQuery_returnsEmpty() {
        // Act
        final Optional<AccountAuthMemberView> result = memberAuthQueryService.findActiveMemberForAuthentication(null);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(securityMemberAccessPort);
    }

    @Test
    @DisplayName("findActiveMemberForAuthentication - 빈 loginId query 전달 시 empty 반환")
    void findActiveMemberForAuthentication_emptyLoginId_returnsEmpty() {
        // Arrange
        final MemberLoginIdQuery query = MemberLoginIdQuery.of("");

        // Act
        final Optional<AccountAuthMemberView> result = memberAuthQueryService.findActiveMemberForAuthentication(query);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(securityMemberAccessPort);
    }

    @Test
    @DisplayName("findActiveMemberForAuthentication - 공백만 있는 loginId query 전달 시 empty 반환")
    void findActiveMemberForAuthentication_blankLoginId_returnsEmpty() {
        // Arrange
        final MemberLoginIdQuery query = MemberLoginIdQuery.of("   ");

        // Act
        final Optional<AccountAuthMemberView> result = memberAuthQueryService.findActiveMemberForAuthentication(query);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(securityMemberAccessPort);
    }

    // === findMemberIdByLoginId ===

    @Test
    @DisplayName("findMemberIdByLoginId - 정상 loginId로 조회 시 memberId 반환")
    void findMemberIdByLoginId_validLoginId_returnsMemberId() {
        // Arrange
        final String loginId = "user01";
        final MemberLoginIdQuery query = MemberLoginIdQuery.of(loginId);
        when(securityMemberAccessPort.findMemberIdByLoginId(loginId))
                .thenReturn(Optional.of(1L));

        // Act
        final Optional<Long> result = memberAuthQueryService.findMemberIdByLoginId(query);

        // Assert
        assertThat(result).isPresent().contains(1L);
        verify(securityMemberAccessPort).findMemberIdByLoginId(loginId);
    }

    @Test
    @DisplayName("findMemberIdByLoginId - 존재하지 않는 loginId 조회 시 empty 반환")
    void findMemberIdByLoginId_nonExistingLoginId_returnsEmpty() {
        // Arrange
        final String loginId = "unknownUser";
        final MemberLoginIdQuery query = MemberLoginIdQuery.of(loginId);
        when(securityMemberAccessPort.findMemberIdByLoginId(loginId))
                .thenReturn(Optional.empty());

        // Act
        final Optional<Long> result = memberAuthQueryService.findMemberIdByLoginId(query);

        // Assert
        assertThat(result).isEmpty();
        verify(securityMemberAccessPort).findMemberIdByLoginId(loginId);
    }

    @Test
    @DisplayName("findMemberIdByLoginId - null query 전달 시 empty 반환")
    void findMemberIdByLoginId_nullQuery_returnsEmpty() {
        // Act
        final Optional<Long> result = memberAuthQueryService.findMemberIdByLoginId(null);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(securityMemberAccessPort);
    }

    @Test
    @DisplayName("findMemberIdByLoginId - 빈 loginId query 전달 시 empty 반환")
    void findMemberIdByLoginId_emptyLoginId_returnsEmpty() {
        // Arrange
        final MemberLoginIdQuery query = MemberLoginIdQuery.of("");

        // Act
        final Optional<Long> result = memberAuthQueryService.findMemberIdByLoginId(query);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(securityMemberAccessPort);
    }
}
