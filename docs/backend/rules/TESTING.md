> 이 문서는 [RULES.md](../RULES.md)에서 분할된 하위 문서입니다. 섹션 번호는 원본과 동일합니다.

---

## 6.2 테스트 코드 작성 규칙 (CRITICAL)

### 전제 조건

| 요구사항                       | 설명                                                          |
|----------------------------|-------------------------------------------------------------|
| **Docker (필수)**            | 통합 테스트는 Testcontainers를 사용하므로 **Docker 데몬이 반드시 실행 중**이어야 한다 |
| JUnit5 + Mockito + AssertJ | 단위 테스트 기본 스택 (`spring-boot-starter-test`에 포함)               |
| Testcontainers 2.0.3       | PostgreSQL 컨테이너 자동 프로비저닝 (`build.gradle.kts`에 선언)           |

> **Docker가 설치되어 있지 않으면 통합 테스트(`apps/*/src/test/`)를 실행할 수 없다.**

---

### 테스트 피라미드

```text
          ╱╲
         ╱  ╲          E2E (미도입)
        ╱────╲         ─────────────────
       ╱      ╲        통합 테스트
      ╱────────╲       (Testcontainers)
     ╱          ╲      ─────────────────
    ╱            ╲     단위 테스트 (기본)
   ╱══════════════╲
```

이 프로젝트는 **2단계 전략**(단위 + 통합)을 운영한다.

| 계층      | 상태                 | 비중 | 피드백 속도     | Docker |
|---------|--------------------|----|------------|--------|
| 단위 테스트  | **기본** (필수)        | 높음 | 빠름 (< 1초)  | 불필요    |
| 통합 테스트  | **선택** (DB/API 흐름) | 중간 | 보통 (5~30초) | **필수** |
| E2E 테스트 | 미도입                | —  | —          | —      |

---

### 단위 테스트 (Unit Test) — 기본

Spring Context를 로딩하지 않고 **순수 Java 코드로 비즈니스 로직을 검증**한다.
새 서비스/Validator/유틸리티를 추가하면 **반드시** 대응하는 단위 테스트를 작성한다.

| 항목             | 규칙                                       |
|----------------|------------------------------------------|
| 스택             | JUnit5 + Mockito + AssertJ               |
| Spring Context | **로딩 없음**                                |
| 위치             | `libs/backend/*/src/test/`               |
| 대상             | 서비스, Validator, 유틸리티, 도메인 로직 전반          |
| Mock 범위        | 외부 의존성(Repository, Port 등)만 `@Mock` 처리   |
| 패턴             | AAA (Arrange-Act-Assert), 메서드당 하나의 동작 검증 |

**단위 테스트로 충분한 로직을 통합 테스트로 작성하지 않는다** (피드백 속도 저하).

#### 단위 테스트 공통 규칙

- **클래스 접근 제한**: 테스트 클래스는 **package-private** (public 금지)
- **final 규칙**: 테스트 코드에서도 재할당 불필요한 변수는 `final` 선언
- **Mockito**: `@Mock` + `@InjectMocks` 조합, `verify()`로 상호작용 검증, `when().thenReturn()`으로 행위 스텁
- **불필요한 스텁 금지**: `strictStubs` 정책 준수

#### 테스트 구조

- 테스트 패키지는 대상 클래스의 패키지 경로와 동일하게 유지
- 테스트 클래스명: `{대상클래스명}Test`
- 테스트 메서드명: `{메서드명}_{시나리오}_{기대결과}` (snake_case)
- **`@Nested` + `@DisplayName` 권장**: 테스트 메서드가 5개 이상이면 `@Nested` 내부 클래스로 논리적 그룹핑한다

```java
@ExtendWith(MockitoExtension.class)
class MemberCreateValidatorTest {

    @Nested
    @DisplayName("validate 메서드")
    class Validate {
        @Test void validate_validRequest_noErrors() { ... }
        @Test void validate_duplicateLoginId_rejectsField() { ... }
    }

    @Nested
    @DisplayName("supports 메서드")
    class Supports {
        @Test void supports_correctClass_returnsTrue() { ... }
        @Test void supports_wrongClass_returnsFalse() { ... }
    }
}
```

---

### 통합 테스트 (Integration Test) — 선택

**DB 연동이 필수인 경우에만** 작성한다: Repository 쿼리 검증, API 전체 흐름 확인, 트랜잭션 동작 등.

| 항목             | 규칙                                                 |
|----------------|----------------------------------------------------|
| 스택             | `@SpringBootTest` + Testcontainers (PostgreSQL)    |
| Spring Context | **전체 로딩**                                          |
| 위치             | `apps/user/src/test/`                              |
| 대상             | Repository 계층, API 엔드포인트, 전체 흐름                    |
| 기반 클래스         | `IntegrationTestBase`를 상속                          |
| 프로필            | `@ActiveProfiles("test")` — `application-test.yml` |
| **Docker**     | **필수** — Docker 데몬이 실행 중이어야 한다                     |

#### Testcontainers 인프라 구조

```text
apps/user/src/test/
├── java/com/example/userapi/
│   ├── config/
│   │   └── TestcontainersConfig.java   # @ServiceConnection + PostgreSQL 컨테이너
│   ├── IntegrationTestBase.java        # @SpringBootTest + @Import + @ActiveProfiles("test")
│   └── HealthCheckIntegrationTest.java # 예시 테스트
└── resources/
    └── application-test.yml            # 테스트 프로필 (Datasource는 Testcontainers 자동 관리)
```

- **`TestcontainersConfig`**: `@ServiceConnection`으로 PostgreSQL 컨테이너를 자동 프로비저닝한다. Datasource URL/username/password를 수동
  설정할 필요 없다.
- **`IntegrationTestBase`**: `@SpringBootTest` + `@Import(TestcontainersConfig.class)` + `@ActiveProfiles("test")`를 선언한
  추상 클래스. 모든 통합 테스트가 이를 상속한다.

#### 통합 테스트 작성 예시

```java
@AutoConfigureMockMvc
class MemberApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createMember_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/members")
                        .header("API-Version", "1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{...}"))
                .andExpect(status().isCreated());
    }
}
```

---

### E2E 테스트 — 미도입

현재 프로젝트에서는 E2E(End-to-End) 테스트를 운영하지 않는다.
브라우저 기반 전체 흐름 검증이 필요해지면, Playwright/Selenium 등을 별도 모듈로 도입한다.

---

### 테스트 실행 명령

```bash
# 개별 모듈 단위 테스트
./gradlew :libs:backend:common:test
./gradlew :libs:backend:global-core:test
./gradlew :libs:backend:security-web:test
./gradlew :libs:backend:domain-core:test
./gradlew :libs:backend:web-support:test

# 특정 테스트 클래스
./gradlew :libs:backend:common:test --tests "com.example.global.utils.PaginationUtilsTest"

# 전체 백엔드 테스트
./gradlew test

# 통합 테스트 (Docker 필수)
./gradlew :apps:user:test
```

### 테스트/설정 파일 변경 규칙

- TDD는 선택 전략, 전체 강제 아님
- 코드 변경 시 위험도 기준으로 필요하면 사전 요청 없이 테스트 추가/수정/실행 가능
- 사용자가 명시 요청하지 않는 한 **설정 파일 임의 수정 금지**
- 설정 변경 필요 시 사유/영향 범위를 먼저 설명하고 확인 후 진행
