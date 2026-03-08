# 백엔드 REST API 개발 규칙

> CRITICAL: 코드 작성/수정이 끝나면 답변 전에 이 지침서(`docs/backend/RULES.md`)를 다시 확인해 규칙 누락이 없는지 최종 점검한다.

이 문서는 AI가 이 모노레포의 **REST API 전용 백엔드**(Spring Boot) 코드를 생성하거나 수정할 때 반드시 따라야 할 규칙입니다.
본 프로젝트는 `apps/*-api`와 `libs/backend/*`를 분리한 모노레포 구조를 기준으로 운영합니다.

> **이 문서는 허브(인덱스)이다.** 상세 규칙은 아래 하위 문서에 분할되어 있으며, 섹션 번호는 원본과 동일하게 유지된다.

---

## 문서 트리거 조건

이 문서를 **언제, 어떤 섹션을** 읽어야 하는지 안내한다.

| 작업 유형             | 읽어야 할 섹션                                                                                                                                         |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| 새 도메인/기능 추가       | §1 전체 → [§2](rules/CODING_CONVENTION.md) → [§3](rules/ARCHITECTURE.md) → [§4](rules/REST_API_SECURITY.md) → [§7](rules/OPERATIONS.md#7-품질-체크리스트) |
| 기존 코드 수정/리팩토링     | §1.1 개발 철학 → 해당 섹션 → [§7](rules/OPERATIONS.md#7-품질-체크리스트)                                                                                        |
| REST API 엔드포인트 추가 | [§4 → §5](rules/REST_API_SECURITY.md) → [§7](rules/OPERATIONS.md#7-품질-체크리스트)                                                                     |
| DTO 작성/변경         | [§2.2 → §2.3 → §2.4](rules/CODING_CONVENTION.md)                                                                                                 |
| 테스트 작성            | [§6.2](rules/TESTING.md)                                                                                                                         |
| 도메인 간 연동          | [§3.3](rules/ARCHITECTURE.md#33-ddd-bounded-context--vertical-slicing--critical)                                                                 |
| 점검 요청             | [§6.3](rules/OPERATIONS.md#63-도메인-지침-점검-요청)                                                                                                      |
| 라이브러리/의존성 변경      | [§6.1](rules/OPERATIONS.md#61-gradle-의존성-점검-실행-규칙-critical)                                                                                      |

---

## 목차

- [§1. 개발 철학 & 프로젝트 개요](#1-개발-철학--프로젝트-개요) *(이 문서)*
    - [§1.1 개발 철학 13대 원칙](#11-개발-철학-13대-원칙)
    - [§1.2 모듈 경계](#12-모듈-경계-module-boundary--critical)
    - [§1.3 프로젝트 구조](#13-모노레포-프로젝트-구조)
    - [§1.4 기술 스택 하한](#14-기술-스택-하한-critical)
    - [§1.5 빌드 명령](#15-빌드-명령)
- [§2. 코딩 컨벤션](rules/CODING_CONVENTION.md) *(하위 문서)*
- [§3. 아키텍처 규칙](rules/ARCHITECTURE.md) *(하위 문서)*
- [§4. REST API 규칙](rules/REST_API_SECURITY.md) *(하위 문서)*
- [§5. 보안 규칙](rules/REST_API_SECURITY.md#5-보안-규칙) *(하위 문서)*
- [§6. 운영 규칙](rules/OPERATIONS.md) *(하위 문서)*
- [§6.2 테스트 규칙](rules/TESTING.md) *(하위 문서)*
- [§7. 품질 체크리스트](rules/OPERATIONS.md#7-품질-체크리스트) *(하위 문서)*
- [§8. 요약 (Cheatsheet)](#8-요약-cheatsheet) *(이 문서)*

---

## 하위 문서 구조

| 하위 문서                                                      | 포함 섹션  | 주요 내용                                            |
|------------------------------------------------------------|--------|--------------------------------------------------|
| [`rules/CODING_CONVENTION.md`](rules/CODING_CONVENTION.md) | §2     | 클린 코드, DTO 전략, JPA, 패키지 구조                       |
| [`rules/ARCHITECTURE.md`](rules/ARCHITECTURE.md)           | §3     | CQRS, 헥사고날, DDD, 전략 패턴, 예외 계층                    |
| [`rules/REST_API_SECURITY.md`](rules/REST_API_SECURITY.md) | §4, §5 | API 버저닝, 응답 구조, 검증, 보안                           |
| [`rules/TESTING.md`](rules/TESTING.md)                     | §6.2   | 테스트 피라미드, 단위/통합 테스트, Testcontainers, Docker 요구사항 |
| [`rules/OPERATIONS.md`](rules/OPERATIONS.md)               | §6, §7 | Gradle 점검, 문서 관리, 체크리스트                          |

---

## 1. 개발 철학 & 프로젝트 개요

### 1.1 개발 철학 13대 원칙

이 프로젝트는 아래 13가지 원칙을 핵심 개발 방향으로 삼는다.
**모든 코드 생성/수정/리뷰 시 아래 원칙을 기준으로 판단**하며, 위반 발견 시 즉시 수정한다.

| 원칙                           | 핵심 요약                                                 |
|------------------------------|-------------------------------------------------------|
| **SRP** (단일 책임 원칙)           | 클래스·메서드는 하나의 책임만 가진다                                  |
| **Clean Code**               | 읽기 쉬운 이름, 짧은 메서드, 명확한 의도                              |
| **CQRS**                     | Command(상태 변경)와 Query(조회)를 물리적으로 분리                   |
| **DDD + Vertical Slicing**   | Bounded Context 경계, 기능 추가는 기술 계층이 아닌 도메인 슬라이스 단위로 자른다 |
| **Hexagonal Architecture**   | Port/Adapter로 도메인을 인프라에서 격리                           |
| **AI 친화적 구조**                | 예측 가능한 패턴, 일관된 네이밍, 자기 문서화 코드                         |
| **Immutability First**       | 상태 변경 최소화, `final`·`record`·방어적 복사 기본값                |
| **Fail Fast**                | 문제를 감추지 않고 즉시 드러낸다, Guard Clause·명시적 예외               |
| **Design by Contract**       | 메서드의 사전·사후 조건과 불변 조건을 명시하여 계약을 코드로 표현한다               |
| **Least Privilege**          | 접근 범위를 최소로 유지한다, `private` 기본값·필요 시에만 공개              |
| **Idempotency**              | 같은 요청을 여러 번 수행해도 결과가 동일하도록 설계한다                       |
| **Module Boundary**          | `libs/backend` 5개 모듈은 단방향 의존, 역방향·순환 의존 금지            |
| **Response DTO Composition** | Response에 필드를 나열하지 않고, 의미 있는 DTO로 묶어 구성한다             |

> **코드를 제공하기 전에 SRP와 CQRS 관점에서 설계를 우선 점검**한다. 위배 가능성이 있으면 이유와 대안을 먼저 설명하고, 합의된 방향으로 코드를 제공한다.

### 1.2 모듈 경계 (Module Boundary) — CRITICAL

`libs/backend`의 5개 모듈은 아래 단방향 의존 관계를 **절대 위반하지 않는다**.

```
common ←── global-core ←── domain-core ←── security-web ←── web-support ←── apps
(순수 공유)    (인프라 공통)     (도메인 로직)      (보안 웹 필터)     (웹 지원)        (앱 진입점)
```

| 모듈             | 책임                                         | 의존 가능 대상                               | 금지                      |
|----------------|--------------------------------------------|----------------------------------------|-------------------------|
| `common`       | 순수 공유 (entity, payload, utils, annotation) | 외부 라이브러리만                              | 다른 모듈 의존 금지             |
| `global-core`  | 인프라 공통 (config, exception, event, logging) | `common`                               | `domain-core` 이상 의존 금지  |
| `domain-core`  | 도메인 로직 + 도메인 간 Port/Adapter                | `common`, `global-core`                | `security-web` 이상 의존 금지 |
| `security-web` | 보안 웹 필터 (인터셉터, Guard)                      | `common`, `global-core`, `domain-core` | `web-support` 의존 금지     |
| `web-support`  | 웹 부가 기능 (이벤트 리스너, AOP)                     | 하위 4개 모듈 전체                            | 없음 (최상위)                |

- **역방향 의존 금지**: `common`이 `global-core`를, `global-core`가 `domain-core`를 참조하는 것은 위반
- **순환 의존 금지**: A → B → A 형태의 의존은 어떤 경우에도 불허
- 모듈 간 통신이 필요하면 **하위 모듈의 인터페이스(Port)를 상위 모듈이 구현**하는 방식으로 해결

### 1.3 모노레포 프로젝트 구조

```
mono-repo-thymeleaf/
├── apps/
│   └── user/               # Spring Boot 4.0.3 + Thymeleaf (Java 25)
├── libs/
│   └── backend/
│       ├── common/             # 순수 공유(entity, payload, utils, annotation, version)
│       ├── global-core/        # 인프라 공통(security, config, exception, event, logging)
│       ├── domain-core/
│       ├── security-web/
│       └── web-support/
├── gradle/wrapper/             # Gradle 9.3.1 Wrapper
├── build.gradle.kts            # Gradle 루트 (백엔드 공통)
├── settings.gradle.kts         # Gradle 서브프로젝트 include
├── docs/
│   ├── backend/                   # 백엔드 상세 가이드
│   │   ├── README.md              # 백엔드 프로젝트 개요
│   │   ├── RULES.md              # 백엔드 개발 규칙 (본 문서 — 허브)
│   │   └── rules/                # 상세 규칙 하위 문서
│   │       ├── CODING_CONVENTION.md  # §2 코딩 컨벤션
│   │       ├── ARCHITECTURE.md       # §3 아키텍처
│   │       ├── REST_API_SECURITY.md  # §4+§5 REST API & 보안
│   │       ├── TESTING.md              # §6.2 테스트 규칙
│   │       └── OPERATIONS.md         # §6+§7 운영 & 체크리스트
│   └── frontend/                  # 프론트엔드 상세 가이드
│       ├── UI_UX_RULES.md        # UI/UX 디자인 지침 (허브)
│       ├── DESIGN_TOKENS.md      # 색상, 타이포, 간격, 라운드, 애니메이션
│       ├── COMPONENTS.md         # 컴포넌트 스타일, 레이아웃, 아이콘
│       ├── PAGE_PATTERNS.md      # 공통 페이지 패턴
│       └── TEMPLATE_CONVENTIONS.md # 기술 스택, 파일 구조, Thymeleaf
```

> 모노레포 경로: `apps/user/`, `libs/backend/*`

### 1.4 기술 스택 하한 (CRITICAL)

프로젝트 베이스라인은 아래로 고정한다. **하위 버전 호환 타협/문법 다운그레이드/레거시 API 재도입은 금지**한다.

| 영역               | 기준                                       |
|------------------|------------------------------------------|
| Java             | **25** (Gradle Toolchain)                |
| Spring Boot      | **4.0.3**                                |
| Spring Framework | **7.x**                                  |
| Gradle           | **9.3.1** (Wrapper)                      |
| QueryDSL         | **7.1** (`io.github.openfeign.querydsl`) |

#### Java 25 문법 우선 지침

- `record`, Pattern Matching(`instanceof`, `switch`), `switch expression` 우선
- Primitive 패턴 매칭(JEP 507): preview 환경에서 검토
- Module Import(JEP 511): 도구성 코드에서만, 운영 코드는 명시적 import
- Compact Source Files(JEP 512): 스파이크/샘플에 한정
- Flexible Constructor Bodies(JEP 513) 스타일 우선
- Text Block(`""" ... """`) + `formatted(...)` 우선
- `ScopedValue`(JEP 506) > `ThreadLocal`
- Virtual Thread + Structured Concurrency(JEP 505) 우선 검토
- AOT cache(JEP 514/515), JFR(JEP 518/520) 활용
- KDF API(JEP 510) 우선, Vector API(JEP 508) 벤치마크 기반
- Preview/Incubator 기능은 합의 + 문서화 필수

#### Spring Framework 7 우선 사용 지침

- API 버저닝: `version = ApiVersioning.*` 사용, 수동 헤더 분기 금지
- 외부 HTTP: `@HttpExchange` 기본, 다수 인터페이스 시 `@EnableHttpServices`
- 입력 검증: Bean Validation + `@InitBinder` + `@RestControllerAdvice`
- null 안정성: `@Nullable`/`Optional` 시그니처 계약

#### Spring Boot 4 우선 사용 지침

- HTTP Service Clients 자동구성, 신규 `RestTemplate` 지양
- `spring.threads.virtual.enabled`: p95/오류율/DB 풀 포화 기준 결정
- 관측성: `spring-boot-starter-opentelemetry` 우선
- 프로퍼티 이름 변경 확인:
    - `management.tracing.enabled` → `management.tracing.export.enabled`
    - `spring.dao.exceptiontranslation.enabled` → `spring.persistence.exceptiontranslation.enabled`

#### JSON (Jackson 3) 주의사항

- Jackson 3: 핵심 패키지 `tools.jackson.*`
- 어노테이션은 `com.fasterxml.jackson.annotation.*` 유지

```java
// ✅ 올바른 import

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.core.JacksonException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// ❌ 금지 (컴파일 안 됨)
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
```

- `JsonNode` 문자열 추출: `stringValue()` 우선 (`asText()` deprecated 가능)
- ObjectMapper는 Spring Bean 주입, `new ObjectMapper()` 지양

### 1.5 빌드 명령

```bash
# 빌드
./gradlew :apps:user:build

# 실행
./gradlew :apps:user:bootRun

# 테스트
./gradlew :apps:user:test

# 라이브러리 단위 테스트
./gradlew :libs:backend:common:test
./gradlew :libs:backend:global-core:test
./gradlew :libs:backend:security-web:test
./gradlew :libs:backend:domain-core:test
./gradlew :libs:backend:web-support:test
```

#### 새 백엔드 API 추가 절차

1. `apps/{name}-api/` 디렉토리를 `user`와 동일 구조로 생성
2. `settings.gradle.kts`에 `include("apps:{name}-api")` 추가
3. `apps/{name}-api/project.json` 생성 (NX 연동)
4. `apps/{name}-api/build.gradle.kts` 생성
5. 포트 번호 변경 (`8082`, `8083`, ...)

#### 모노레포 Gradle 경로 규칙

- Gradle 명령 시 서브프로젝트 경로 명시: `./gradlew :apps:user:build`
- NX 경유: `pnpm nx build user`

---

## 8. 요약 (Cheatsheet)

> 상세 규칙은 각 섹션의 [하위 문서](#하위-문서-구조)를 참조한다.

| 구분            | 규칙                                                                                                   | 상세 섹션                              |
|---------------|------------------------------------------------------------------------------------------------------|------------------------------------|
| DTO           | record + `from/of`, 외부 `new` 금지                                                                      | [§2.2](rules/CODING_CONVENTION.md) |
| Response 구조화  | 필드 나열 금지, 의미 단위 DTO로 묶어 조합 (3개 이하 단순 응답은 예외)                                                         | [§2.3](rules/CODING_CONVENTION.md) |
| 계층 경계         | 값 나열 금지, DTO 1개로 전달                                                                                  | [§2.4](rules/CODING_CONVENTION.md) |
| 도메인 경계        | Port/Event/DTO/ID로만 참조, Repository·Entity·Service 직접 참조 금지                                           | [§3.3](rules/ARCHITECTURE.md)      |
| 헥사고날 아키텍처     | 의존 방향 항상 안쪽으로, Port/Adapter로 도메인-인프라 격리                                                              | [§3.2](rules/ARCHITECTURE.md)      |
| AI 친화적 구조     | 예측 가능한 네이밍(`{Domain}{역할}{계층}`), 일관된 패키지, 자기 문서화 코드                                                   | [§3.6](rules/ARCHITECTURE.md)      |
| 스크립트          | `gradlew`/`gradlew.bat` 외 shell 스크립트 추가/수정은 사용자 요청 시만 진행                                             | [§6.4](rules/OPERATIONS.md)        |
| CQRS          | 물리 분리, Command=`@Transactional`, Query=`readOnly=true`                                               | [§3.1](rules/ARCHITECTURE.md)      |
| 조회 최적화        | QueryDSL + fetch join, DTO Projection                                                                | [§2.6](rules/CODING_CONVENTION.md) |
| 로깅            | traceId 포함, 민감정보 금지                                                                                  | [§2.1](rules/CODING_CONVENTION.md) |
| API 버전        | `version = ApiVersioning.*`, 기본 `0.0`(무효), `API-Version` 헤더 필수                                       | [§4.1](rules/REST_API_SECURITY.md) |
| 컨트롤러          | `RestApiController` 응답, 서비스에서 `ResponseEntity` 금지                                                    | [§4.3](rules/REST_API_SECURITY.md) |
| 컨트롤러 격리       | 멀티 앱 시 앱 전용 컨트롤러에 `@ConditionalOnProperty(name = "app.type")` 추가                                     | [§4.3](rules/REST_API_SECURITY.md) |
| 설정 변경         | 설정 변경 사유/영향 범위를 먼저 설명하고 확인 후 진행                                                                      | [§6.4](rules/OPERATIONS.md)        |
| 테스트           | 단위 테스트(기본) + 통합 테스트(Testcontainers, 선택), Docker 필수                                                   | [§6.2](rules/TESTING.md)           |
| Enum 계약 동기화   | `Api* == Domain name()` 유지 + 빌드 시 TS 자동 생성(`generateContractEnumTs`) + `pnpm nx test domain-core` 통과 | [§7](rules/OPERATIONS.md)          |
| 외부 연동         | SDK → `@HttpExchange` → `@EnableHttpServices`                                                        | §1.4                               |
| 보안            | `@PreAuthorize`만, 누락=공개                                                                              | [§5](rules/REST_API_SECURITY.md)   |
| 인증            | HttpSession + 쿠키 기반, CSRF 활성화 (API 제외)                                                               | [§5](rules/REST_API_SECURITY.md)   |
| JPA           | `LAZY` 명시, `EAGER` 금지                                                                                | [§2.6](rules/CODING_CONVENTION.md) |
| 멀티라인          | `"\n"` 금지, Text Block 사용                                                                             | [§2.1](rules/CODING_CONVENTION.md) |
| InitBinder    | DTO 1:1 매칭, 공용 이름 금지, `supports()` 방어                                                                | [§4.4](rules/REST_API_SECURITY.md) |
| 검증            | Bean Validation + InitBinder Validator, 서비스는 최종 보장만                                                  | [§4.4](rules/REST_API_SECURITY.md) |
| Java 25       | record/pattern matching/switch 우선 + ScopedValue/Virtual Thread                                       | §1.4                               |
| Spring 7      | API Versioning + `@HttpExchange` 우선                                                                  | §1.4                               |
| Spring Boot 4 | HTTP Service Clients/Virtual Thread/OpenTelemetry 우선                                                 | §1.4                               |
| 버전 하한         | Java 25 + Boot 4 + Framework 7 미만 호환 타협 금지                                                           | §1.4                               |
| Jackson 3     | `tools.jackson.*`, 어노테이션만 `com.fasterxml.jackson.annotation.*`                                       | §1.4                               |
| Logback       | XML 설정 파일 미사용                                                                                        | [§2.1](rules/CODING_CONVENTION.md) |
| Serena 메모리    | `.serena/memories/`는 git 공유, `.serena/cache/`는 로컬 전용                                                 | [§6.6](rules/OPERATIONS.md)        |
| 의존성 문서        | `build.gradle.kts` 의존성 변경 시 `docs/backend/BACKEND_DEPENDENCIES.md`도 반드시 함께 갱신                        | [§6.4](rules/OPERATIONS.md)        |
