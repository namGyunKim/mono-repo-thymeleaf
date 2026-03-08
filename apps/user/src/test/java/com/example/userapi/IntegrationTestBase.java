package com.example.userapi;

import com.example.userapi.config.TestcontainersConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 통합 테스트 기반 클래스.
 * Testcontainers로 PostgreSQL을 자동 프로비저닝한다.
 *
 * <p>사용법: 이 클래스를 상속하고 테스트 메서드를 작성한다.</p>
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
abstract class IntegrationTestBase {
}
