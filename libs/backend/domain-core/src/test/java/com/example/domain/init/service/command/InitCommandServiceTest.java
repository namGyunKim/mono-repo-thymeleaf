package com.example.domain.init.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.init.support.InitMemberSeedCommand;
import com.example.domain.init.support.InitMemberSeedPort;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InitCommandServiceTest {

    @InjectMocks
    private InitCommandService initCommandService;

    @Mock
    private InitMemberSeedPort initMemberSeedPort;

    @Test
    @DisplayName("initOnApplicationReady는 역할별 시드를 모두 실행한다")
    void initOnApplicationReady_all_roles_seeds_all_members() {
        // Arrange
        given(initMemberSeedPort.existsByRole(AccountRole.SUPER_ADMIN)).willReturn(false);
        given(initMemberSeedPort.existsByRole(AccountRole.ADMIN)).willReturn(false);
        given(initMemberSeedPort.existsByRole(AccountRole.USER)).willReturn(false);
        given(initMemberSeedPort.seedMember(any(InitMemberSeedCommand.class))).willReturn(1L);

        // Act
        initCommandService.initOnApplicationReady();

        // Assert — SUPER_ADMIN 1 + ADMIN 10 + USER 51 = 62
        verify(initMemberSeedPort, times(62)).seedMember(any(InitMemberSeedCommand.class));
    }

    @Test
    @DisplayName("이미 존재하는 역할은 시드를 생략한다")
    void initOnApplicationReady_existing_roles_skips_seeding() {
        // Arrange
        given(initMemberSeedPort.existsByRole(AccountRole.SUPER_ADMIN)).willReturn(true);
        given(initMemberSeedPort.existsByRole(AccountRole.ADMIN)).willReturn(true);
        given(initMemberSeedPort.existsByRole(AccountRole.USER)).willReturn(true);

        // Act
        initCommandService.initOnApplicationReady();

        // Assert
        verify(initMemberSeedPort, never()).seedMember(any(InitMemberSeedCommand.class));
    }

    @Test
    @DisplayName("일부 역할만 존재하면 해당 역할만 생략한다")
    void initOnApplicationReady_partial_roles_seeds_missing_only() {
        // Arrange
        given(initMemberSeedPort.existsByRole(AccountRole.SUPER_ADMIN)).willReturn(true);
        given(initMemberSeedPort.existsByRole(AccountRole.ADMIN)).willReturn(false);
        given(initMemberSeedPort.existsByRole(AccountRole.USER)).willReturn(true);
        given(initMemberSeedPort.seedMember(any(InitMemberSeedCommand.class))).willReturn(1L);

        // Act
        initCommandService.initOnApplicationReady();

        // Assert — ADMIN 10명만 생성
        verify(initMemberSeedPort, times(10)).seedMember(any(InitMemberSeedCommand.class));
    }
}
