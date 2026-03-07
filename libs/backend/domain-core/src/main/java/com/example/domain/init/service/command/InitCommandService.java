package com.example.domain.init.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.init.support.InitMemberSeedCommand;
import com.example.domain.init.support.InitMemberSeedPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@Profile("local")
public class InitCommandService {

    private static final int ADMIN_SEED_COUNT = 10;
    private static final int USER_SEED_COUNT = 51;

    private final InitMemberSeedPort initMemberSeedPort;

    @EventListener(ApplicationReadyEvent.class)
    public void initOnApplicationReady() {
        seedSuperAdmin();
        seedAdmins();
        seedUsers();
    }

    private void seedSuperAdmin() {
        if (initMemberSeedPort.existsByRole(AccountRole.SUPER_ADMIN)) {
            log.info("[Init] SUPER_ADMIN 계정이 이미 존재합니다. 시드 생략");
            return;
        }

        initMemberSeedPort.seedMember(InitMemberSeedCommand.of("superAdmin", "최고관리자", "1234", AccountRole.SUPER_ADMIN));
        log.info("[Init] SUPER_ADMIN 기본 계정 생성 완료 (loginId=superAdmin)");
    }

    private void seedAdmins() {
        if (initMemberSeedPort.existsByRole(AccountRole.ADMIN)) {
            log.info("[Init] ADMIN 계정이 이미 존재합니다. 시드 생략");
            return;
        }

        for (int i = 1; i <= ADMIN_SEED_COUNT; i++) {
            initMemberSeedPort.seedMember(InitMemberSeedCommand.of("admin" + i, "관리자" + i, "1234", AccountRole.ADMIN));
        }
        log.info("[Init] ADMIN 계정 {}명 생성 완료 (loginId=admin1~admin{})", ADMIN_SEED_COUNT, ADMIN_SEED_COUNT);
    }

    private void seedUsers() {
        if (initMemberSeedPort.existsByRole(AccountRole.USER)) {
            log.info("[Init] USER 계정이 이미 존재합니다. 시드 생략");
            return;
        }

        for (int i = 1; i <= USER_SEED_COUNT; i++) {
            initMemberSeedPort.seedMember(InitMemberSeedCommand.of("user" + i, "유저이름" + i, "1234", AccountRole.USER));
        }
        log.info("[Init] USER 계정 {}명 생성 완료 (loginId=user1~user{})", USER_SEED_COUNT, USER_SEED_COUNT);
    }
}
