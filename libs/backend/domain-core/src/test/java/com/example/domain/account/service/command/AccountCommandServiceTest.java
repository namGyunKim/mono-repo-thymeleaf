package com.example.domain.account.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountActivityPublishCommand;
import com.example.domain.account.payload.dto.AccountLogoutCommand;
import com.example.domain.account.payload.dto.AccountProfileUpdateCommand;
import com.example.domain.account.payload.dto.AccountWithdrawCommand;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.account.support.AccountActivityPublishPort;
import com.example.domain.account.support.AccountMemberCommandPort;
import com.example.domain.member.enums.MemberType;
import com.example.global.exception.GlobalException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountCommandServiceTest {

    @InjectMocks
    private AccountCommandService accountCommandService;

    @Mock
    private AccountMemberCommandPort accountMemberCommandPort;

    @Mock
    private AccountActivityPublishPort accountActivityPublishPort;

    private CurrentAccountDTO createCurrentAccount() {
        return CurrentAccountDTO.of(1L, "testUser", "testNick", AccountRole.USER, MemberType.GENERAL);
    }

    // ===== logout =====

    @Test
    @DisplayName("logout에 null을 전달하면 GlobalException이 발생한다")
    void logout_null_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> accountCommandService.logout(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("로그아웃 요청 값이 비어있습니다.");
    }

    @Test
    @DisplayName("logout은 활동 로그를 발행한다")
    void logout_publishes_activity() {
        // Arrange
        final CurrentAccountDTO currentAccount = createCurrentAccount();
        final AccountLogoutCommand command = AccountLogoutCommand.of(currentAccount);

        // Act
        accountCommandService.logout(command);

        // Assert
        verify(accountActivityPublishPort).publishMemberActivity(any(AccountActivityPublishCommand.class));
    }

    // ===== withdraw =====

    @Test
    @DisplayName("withdraw에 null을 전달하면 GlobalException이 발생한다")
    void withdraw_null_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> accountCommandService.withdraw(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("탈퇴 요청 값이 비어있습니다.");
    }

    @Test
    @DisplayName("withdraw는 회원을 비활성화한다")
    void withdraw_deactivates_member() {
        // Arrange
        final CurrentAccountDTO currentAccount = createCurrentAccount();
        final AccountWithdrawCommand command = AccountWithdrawCommand.of(currentAccount);

        // Act
        accountCommandService.withdraw(command);

        // Assert
        verify(accountMemberCommandPort).deactivateMember(eq(currentAccount.role()), eq(currentAccount.id()));
    }

    // ===== updateProfile =====

    @Test
    @DisplayName("updateProfile에 null을 전달하면 GlobalException이 발생한다")
    void updateProfile_null_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> accountCommandService.updateProfile(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("프로필 수정 요청 값이 비어있습니다.");
    }

    @Test
    @DisplayName("updateProfile은 포트에 위임하고 결과 ID를 반환한다")
    void updateProfile_delegates_to_port() {
        // Arrange
        final CurrentAccountDTO currentAccount = createCurrentAccount();
        final AccountProfileUpdateCommand command = AccountProfileUpdateCommand.of(
                currentAccount, "newNick", "newPassword"
        );
        final Long expectedId = 100L;
        given(accountMemberCommandPort.updateMemberProfile(command)).willReturn(expectedId);

        // Act
        final Long result = accountCommandService.updateProfile(command);

        // Assert
        assertThat(result).isEqualTo(expectedId);
        verify(accountMemberCommandPort).updateMemberProfile(command);
    }
}
