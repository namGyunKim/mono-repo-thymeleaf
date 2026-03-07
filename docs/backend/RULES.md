# 백엔드 REST API 개발 규칙

> CRITICAL: 코드 작성/수정이 끝나면 답변 전에 이 지침서(`docs/backend/RULES.md`)를 다시 확인해 규칙 누락이 없는지 최종 점검한다.

이 문서는 AI가 이 모노레포의 **REST API 전용 백엔드**(Spring Boot) 코드를 생성하거나 수정할 때 반드시 따라야 할 규칙입니다.
본 프로젝트는 `apps/*-api`와 `libs/backend/*`를 분리한 모노레포 구조를 기준으로 운영합니다.

---

## 문서 트리거 조건

이 문서를 **언제, 어떤 섹션을** 읽어야 하는지 안내한다.

| 작업 유형             | 읽어야 할 섹션                        |
|-------------------|---------------------------------|
| 새 도메인/기능 추가       | §1 전체 → §2 → §3 → §4 → §7 체크리스트 |
| 기존 코드 수정/리팩토링     | §1.1 개발 철학 → 해당 섹션 → §7 체크리스트   |
| REST API 엔드포인트 추가 | §4 → §5 → §7 체크리스트              |
| DTO 작성/변경         | §2.2 → §2.3 → §2.4              |
| 테스트 작성            | §6.2                            |
| 도메인 간 연동          | §3.3 DDD Bounded Context        |
| 점검 요청             | §6.3 도메인 지침 점검                  |
| 라이브러리/의존성 변경      | §6.1 Gradle 의존성 점검              |

---

## 목차

- [§1. 개발 철학 & 프로젝트 개요](#1-개발-철학--프로젝트-개요)
    - [§1.1 개발 철학 13대 원칙](#11-개발-철학-13대-원칙)
    - [§1.2 모듈 경계](#12-모듈-경계-module-boundary--critical)
    - [§1.3 프로젝트 구조](#13-모노레포-프로젝트-구조)
    - [§1.4 기술 스택 하한](#14-기술-스택-하한-critical)
    - [§1.5 빌드 명령](#15-빌드-명령)
- [§2. 코딩 컨벤션](#2-코딩-컨벤션-coding-convention)
    - [§2.1 클린 코드 & 기본 원칙](#21-클린-코드--기본-원칙)
    - [§2.2 DTO 전략](#22-dto-전략-record--static-factory--critical)
    - [§2.3 Response DTO Composition](#23-response-dto-composition-응답-구조화--critical)
    - [§2.4 파라미터 전달 원칙](#24-파라미터-전달-원칙-dto-우선--critical)
    - [§2.5 객체 생성 및 변경](#25-객체-생성-및-변경)
    - [§2.6 JPA & Database](#26-jpa--database)
    - [§2.7 패키지 구조](#27-패키지-구조-rest-api-전용)
- [§3. 아키텍처 규칙](#3-아키텍처-규칙-architecture-rules)
    - [§3.1 CQRS](#31-cqrs-command--query-분리--critical)
    - [§3.2 Hexagonal Architecture](#32-hexagonal-architecture-ports--adapters--critical)
    - [§3.3 DDD Bounded Context](#33-ddd-bounded-context--vertical-slicing--critical)
    - [§3.4 전략 패턴](#34-전략-패턴-strategy-pattern--critical)
    - [§3.5 예외 계층 정책](#35-예외-계층-정책-exception-hierarchy)
    - [§3.6 이벤트 기반 로깅](#36-이벤트-기반-로깅)
- [§4. REST API 규칙](#4-rest-api-규칙)
    - [§4.1 API-Version 헤더](#41-api-version-헤더-규칙-critical)
    - [§4.2 응답 구조 & 멱등성](#42-응답-구조--멱등성)
    - [§4.3 컨트롤러 작성 원칙](#43-컨트롤러-작성-원칙)
    - [§4.4 검증 & 예외 처리](#44-검증--예외-처리-critical)
- [§5. 보안 규칙](#5-보안-규칙)
- [§6. 운영 규칙](#6-운영-규칙)
    - [§6.1 Gradle 의존성 점검](#61-gradle-의존성-점검-실행-규칙-critical)
    - [§6.2 테스트 코드 작성](#62-테스트-코드-작성-규칙-critical)
    - [§6.3 도메인 지침 점검](#63-도메인-지침-점검-요청)
    - [§6.4 문서 & 설정 관리](#64-문서--설정-관리)
    - [§6.5 AI 자율 실행 규칙](#65-ai-자율-실행-규칙-critical)
    - [§6.6 Serena 메모리 관리](#66-serena-메모리-관리-규칙)
- [§7. 품질 체크리스트](#7-품질-체크리스트)
- [§8. 요약 (Cheatsheet)](#8-요약-cheatsheet)

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
│   │   └── RULES.md              # 백엔드 개발 규칙 (본 문서)
│   └── frontend/                  # 프론트엔드 상세 가이드
│       └── UI_UX_RULES.md        # UI/UX 디자인 지침
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

## 2. 코딩 컨벤션 (Coding Convention)

### 2.1 클린 코드 & 기본 원칙

#### 언어 및 소통

- 모든 답변, 주석, 커밋 메시지는 **한국어(Korean)**
- 본 문서는 **REST API 전용** — 화면(UI)/템플릿/정적 리소스 변경 금지
- 요청이 모호하면 **추정하지 말고 질문**

#### 레거시/호환성 정책

- 호환성 목적의 레거시 코드를 남기지 않는다
- 변경 시 기존 방식을 제거하고 최신 규칙으로 통일

#### 클린 코드 & SRP

- 읽기 쉬운 이름, 짧은 메서드, 명확한 책임
- 가독성은 성능 미세 최적화보다 우선
- "파일 1개 = public 타입 1개" 원칙
- 중첩 깊이 2단계 이내, 초과 시 guard clause 또는 메서드 분리
- **Guard Clause 패턴**: 메서드 초입에서 유효하지 않은 조건을 `early return`/`early throw`로 처리하여 정상 흐름의 들여쓰기를 최소화한다

```java
// ✅ Guard Clause
public void doSomething(final String input) {
    if (input == null || input.isBlank()) {
        throw new IllegalArgumentException("input은 필수입니다.");
    }
    // 정상 로직 (들여쓰기 1단계)
}

// ❌ 중첩 분기
public void doSomething(final String input) {
    if (input != null && !input.isBlank()) {
        // 정상 로직 (불필요한 들여쓰기 2단계)
    }
}
```

- 공개 메서드 파라미터 3개 이상이면 전용 DTO 도입 고려
- 조회 메서드는 side-effect 금지, 명령 메서드는 상태 변경이 드러나는 이름
- `null` 반환 최소화, 컬렉션은 빈 컬렉션 반환
- **`Optional`은 반환 타입 전용** — 메서드 파라미터, 필드, 컬렉션 원소에 `Optional` 사용 금지

```java
// ✅ 반환 타입으로만 사용
public Optional<Member> findById(final Long id) { ... }

// ❌ 파라미터에 사용 금지
public void update(Optional<String> name) { ... }

// ❌ 필드에 사용 금지
private Optional<String> nickname;
```

- 계층 의존 방향: `api -> service -> repository`, 역방향/순환 금지
- 권장 기준: public 클래스 300라인, 메서드 30라인 초과 시 분리 고려
- 재할당 불필요한 변수는 **`final` 기본값**
- 람다가 단순 위임이면 메서드 레퍼런스 우선
- **메서드 정렬 순서**: `public` → `protected` → `private` (접근 제한자 기준 내림차순)
    - 같은 접근 수준 내에서는 호출 순서(위에서 아래로 읽히는 순서)를 따른다
    - 생성자·정적 팩토리는 클래스 최상단 배치

#### 계약 기반 설계 (Design by Contract)

- **Port 인터페이스**에는 Javadoc으로 사전 조건(파라미터 제약), 사후 조건(반환 보장), 예외 조건을 명시한다

```java
/**
 * 로그인 ID로 회원을 조회한다.
 *
 * @param loginId null이 아닌 로그인 ID
 * @return 해당 회원 정보, 존재하지 않으면 empty
 * @throws IllegalArgumentException loginId가 null인 경우
 */
Optional<LoginMemberView> findByLoginId(String loginId);
```

- 공개 메서드의 파라미터 제약은 **Guard Clause로 강제**한다 (Fail Fast와 연계)
- 반환 타입이 계약이다 — `Optional`이면 "없을 수 있음", `List`이면 "null 아닌 빈 리스트 보장"
- 계약은 코드(타입 시스템 + 검증)로 표현하고, 주석은 보조 수단이다

#### 최소 권한 원칙 (Least Privilege)

- **접근 제한자 기본값은 `private`** — 외부에 공개할 명확한 이유가 있을 때만 `public`/`protected`로 열기
- 클래스도 동일 — 패키지 외부에서 사용하지 않으면 `package-private`(접근 제한자 생략) 유지
- 필드는 항상 `private`, 엔티티 기본 생성자는 `protected`
- 유틸리티 클래스는 `private` 생성자로 인스턴스화 차단 (`final class` + `private` 생성자)
- 메서드가 `static`이면서 상태에 의존하지 않으면 `private static`으로 범위 최소화

#### 관측성/로깅 기준

- 예외/요청 로그에 **traceId** 필수
- WARN/ERROR 로그에 핵심 컨텍스트 포함
- 민감정보(password/token/secret) 로그 금지
- `authorization` 계열 필드는 마스킹 대상
- 에러 응답 `requestId`에 traceId 값 포함
- 로그/MDC/헤더 키는 **traceId** 통일, 헤더는 `X-Trace-Id`
- 로그 템플릿은 Text Block 기반 멀티라인 우선
- 예외 로그 템플릿 변경은 `ExceptionLogTemplates`에서만 관리
- traceId는 **MDC 패턴(`%X{traceId}`)이 자동 출력**하므로, 로그 메시지 본문에 `traceId={}`를 수동 삽입하지 않는다
- 서비스/도메인 계층에서 `TraceIdUtils.resolveTraceId()`를 로깅 목적으로 직접 호출하지 않는다 (이벤트 데이터 전달 등 구조적 용도 제외)
- **보안 상태 변경**(로그아웃, 토큰 폐기, 권한 변경 등)은 반드시 **INFO 레벨로 로깅**한다 — 감사 추적(audit trail) 목적
- `finally` 블록에서 `RequestContextHolder.currentRequestAttributes()` 접근 시 `try-catch(IllegalStateException)`으로 방어한다
- **SLF4J 플레이스홀더(`{}`) 강제** — 로그 메시지에 문자열 연결(`+`) 사용 금지

```java
// ✅ 플레이스홀더 사용
log.info("회원 생성 완료: memberId={}, role={}", memberId, role);

// ❌ 문자열 연결 금지
log.info("회원 생성 완료: memberId=" + memberId + ", role=" + role);
```

#### 로그 레벨 사용 기준

| 레벨        | 사용 기준                              | 예시                                         |
|-----------|------------------------------------|--------------------------------------------|
| **ERROR** | 시스템이 정상 동작할 수 없는 예상 외 예외, 즉시 대응 필요 | 예상 외 런타임 예외, DB 연결 실패, 외부 인프라 장애           |
| **WARN**  | 비즈니스 예외, 복구 가능한 오류, 주의가 필요한 상황     | 인증 실패, 잘못된 요청, 외부 API 호출 실패(재시도 가능), 검증 실패 |
| **INFO**  | 주요 비즈니스 흐름, 상태 변경, 정상 처리 완료        | 로그인 성공, 외부 API 호출 완료, 초기화 완료               |
| **DEBUG** | 개발/디버깅용 상세 정보, 운영에서는 비활성화          | 쿼리 파라미터 상세, 중간 처리 결과, 조건 분기 경로             |

- ERROR는 **운영 알림 대상**, WARN은 **모니터링 대상**으로 구분한다
- 외부 API 호출 실패: 재시도 가능하면 WARN, 전체 흐름 실패로 이어지면 ERROR
- 비즈니스 예외(`BaseAppException` 계열)는 기본 **WARN**, 예상 외 예외는 **ERROR**

#### 멀티라인 문자열 (Text Block)

- ❌ `"\n"` escape 금지
- ✅ Text Block(`""" ... """`) + `formatted(...)` 사용

#### 기술 스택 부가 규칙

- 외부 연동 우선순위: 공식 SDK → `@HttpExchange` → `@EnableHttpServices`
- **Logback XML 설정 파일 미사용** (`logback.xml`, `logback-spring.xml` 등 추가/수정 금지)

### 2.2 DTO 전략 (Record + Static Factory) — CRITICAL

- DTO는 무조건 `record`
- 내부에 정적 팩토리(`from`/`of`) 필수
- 🚨 외부 `new DTO(...)` 직접 호출 금지 — 오직 `DTO.from(...)`/`DTO.of(...)` 만
- `from(...)`: 다른 객체 → DTO, `of(...)`: 원시값 → DTO
- DTO 네이밍: `CreateRequest`, `UpdateCommand`, `DetailResponse`, `ListQuery`
- DTO는 검증/매핑 외 비즈니스 로직 불포함
- **Record 컬렉션 필드 방어적 복사**: record의 `List`/`Set`/`Map` 필드는 Compact Constructor에서 `List.copyOf()` 등으로 불변 복사한다

```java
public record MemberListResponse(List<MemberSummary> members) {
    public MemberListResponse {
        members = List.copyOf(members);  // 방어적 복사
    }
}
```

#### DTO 생성 규칙 가이드 (상세)

목적: DTO 생성 시점과 역할을 고정해, DTO 구조 변경 시 수정 범위를 DTO 내부로 한정한다.

핵심 규칙:

1. DTO는 `record`로 작성한다.
2. 외부에서는 생성자를 직접 호출하지 않는다.
3. `of(...)`는 원시값/직접 값 생성에만 사용한다.
4. `from(...)`은 다른 객체를 DTO로 변환할 때만 사용한다.
5. 변환 로직은 DTO 내부로 모은다.

예시:

```java
public record MemberSummaryResponse(Long id, String name) {
    public static MemberSummaryResponse of(Long id, String name) {
        return new MemberSummaryResponse(id, name);
    }

    public static MemberSummaryResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberSummaryResponse(member.getId(), member.getName());
    }
}
```

적용 팁:

1. 호출부에서는 `DTO.of(...)` 또는 `DTO.from(...)`만 사용한다.
2. DTO 구조 변경 시 DTO 내부에서만 수정되도록 유지한다.

계정 도메인 예시:

1. 직접 값 생성은 `of(...)`로 통일한다.
2. 엔티티/프로젝션 변환은 `from(...)`만 사용한다.

```java
AccountLoginIdQuery query = AccountLoginIdQuery.of(loginId);
LoginMemberResponse response = LoginMemberResponse.from(member);
LoginMemberResponse projectionResponse = LoginMemberResponse.from(view);
```

### 2.3 Response DTO Composition (응답 구조화) — CRITICAL

Response에 필드를 평면적으로 나열하지 않고, **관련 필드를 의미 있는 DTO로 묶어 구성**한다.

#### 원칙

- Response는 **데이터의 의미 단위(DTO)를 조합**하여 구성한다
- 도메인 정보, 메타 정보, 연관 정보 등 성격이 다른 필드를 하나의 Response에 혼합하지 않는다
- 내부 DTO를 재사용하면 응답 구조 변경 시 수정 범위가 DTO 내부로 한정된다

#### 적용 기준

| 상황                              | 전략                                                  |
|---------------------------------|-----------------------------------------------------|
| 필드 3개 이하의 단순 응답                 | 평면 나열 허용 (`IdResponse`, `SocialRedirectResponse` 등) |
| 필드 4개 이상 또는 의미 단위가 2개 이상        | 관련 필드를 DTO로 묶어 구성                                   |
| 외부 API 응답 매핑 (`client/payload`) | 외부 스펙 그대로 매핑 허용 (내부 Response에는 변환 적용)               |

#### 예시

```java
// ❌ 필드 나열 — 회원 정보와 주문 정보가 혼합
public record OrderSummaryResponse(
                Long id, String loginId, String nickName,
                Long orderId, String orderStatus, int totalPrice
        ) {
}

// ✅ DTO 조합 — 관심사별 분리
public record OrderSummaryResponse(
        MemberSummaryResponse member,
        OrderDetailResponse order
) {
    public static OrderSummaryResponse of(
            final MemberSummaryResponse member,
            final OrderDetailResponse order
    ) {
        return new OrderSummaryResponse(member, order);
    }
}
```

#### 기존 Response 점검 가이드

신규 Response 작성 또는 기존 Response 수정 시, 아래를 확인한다:

1. 필드가 4개 이상이면 **의미 단위로 묶을 수 있는 그룹**이 있는지 확인
2. 이미 존재하는 DTO(`MemberProfileDetailResponse` 등)를 **재사용**할 수 있는지 확인
3. 조합된 Response는 `of(...)` 정적 팩토리로 생성

### 2.4 파라미터 전달 원칙 (DTO 우선) — CRITICAL

- 계층 경계에서 값을 개별 전달하지 말고 **DTO로 묶어 전달**
- 예외: `JpaRepository` 기본 메서드(`findById`, `save` 등)는 DTO 없이 사용
- Repository: `(검색조건 DTO 1개) + (Pageable 1개)` 패턴 표준

### 2.5 객체 생성 및 변경

- ❌ Lombok `@Builder`, `@Setter`, `@Data` 금지
- ✅ Lombok 허용: `@Slf4j`, `@RequiredArgsConstructor`, `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- ❌ `System.out.println` 금지 → Logger 사용
- ✅ 생성자 또는 정적 팩토리(`of`, `from`, `create`) 우선
- ✅ 엔티티 기본 생성자는 `protected`, 생성 로직은 정적 팩토리
- ✅ Setter 대신 의도를 드러내는 변경 메서드 (`changePassword(...)`, `activate()`)
- ❌ JPA 엔티티에 `equals()`/`hashCode()` 재정의 금지 — JPA 기본 identity(인스턴스 동일성)를 사용한다
    - Proxy, Lazy Loading, detached 상태에서 예기치 않은 동작을 방지
    - 비교가 필요하면 ID 필드를 직접 비교한다

#### 민감정보 마스킹

```java
public record LoginRequest(
        String loginId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {
}
```

### 2.6 JPA & Database

- Dirty Checking 우선, 불필요한 `repository.save()` 지양
- 연관관계 기본 `fetch = FetchType.LAZY` **명시**, `EAGER` 금지
- Cascade/orphanRemoval은 Aggregate Root에서만
- 컬럼/테이블 코멘트: `@Column(comment=...)` / `@Table(comment=...)`
- 단순/정적 조회는 파생 쿼리(Derived Query) 우선, 동적/복잡 조회는 QueryDSL 강제
- Fetch Join으로 N+1 방지 (페이징 시 주의)
- 조회는 DTO Projection 우선
- 벌크 쿼리 시 `flush/clear` 고려
- Command: 엔티티 조회 우선 / Query: DTO 프로젝션 우선
- 총 건수 불필요하면 `Page` 대신 `Slice`
- `exists`/`count`는 전용 쿼리로 처리
- Enum 변경 시 DB 제약조건 동기화 + ALTER SQL 함께 제공

#### Repository 메서드 네이밍 규칙

| 접두사              | 반환 타입                           | 용도                | 예시                           |
|------------------|---------------------------------|-------------------|------------------------------|
| `findBy*`        | `Optional<Entity>`              | 단건 엔티티 조회         | `findByLoginId(String)`      |
| `findAllBy*`     | `List<Entity>` / `Page<Entity>` | 다건 엔티티 조회         | `findAllByRole(AccountRole)` |
| `findProjected*` | `Optional<DTO>` / `List<DTO>`   | DTO Projection 조회 | `findProjectedById(Long)`    |
| `exists*`        | `boolean`                       | 존재 여부 확인          | `existsByLoginId(String)`    |
| `countBy*`       | `long`                          | 건수 조회             | `countByRole(AccountRole)`   |

- ❌ `getBy*` 접두사 사용 금지 — `findBy*`로 통일
- JpaRepository 기본 메서드(`findById`, `save`, `delete` 등)는 그대로 사용

#### 파생 쿼리(Derived Query) vs `@Query` 선택 기준

Spring Data JPA 2025.1부터 파생 쿼리가 Criteria API 대신 **JPQL 문자열로 변환**되어 Hibernate의 **Query Structure Caching** 혜택을 받는다.
동일 쿼리 재실행 시 파싱/컴파일을 건너뛰므로 처리량이 약 **25% 향상**된다 (인메모리 DB 기준 최대 3.5배).

| 상황               | 선택                        | 이유                                   |
|------------------|---------------------------|--------------------------------------|
| 조건 1~2개, 정적 조회   | 파생 쿼리 (`findByLoginId`)   | Query Structure Caching 자동 적용, 코드 간결 |
| JOIN/서브쿼리/복잡한 조건 | `@Query` JPQL             | 파생 쿼리로 표현 불가하거나 가독성 저하               |
| 동적 조건 조합         | QueryDSL                  | 런타임 조건 분기 필요                         |
| DTO Projection   | `@Query` JPQL 또는 QueryDSL | 파생 쿼리는 엔티티 반환만 지원                    |

> 파생 쿼리로 충분한 경우 `@Query`로 재작성하지 않는다 — 캐싱 효율이 동일하면서 메서드 시그니처만으로 의도가 드러나는 파생 쿼리가 유지보수에 유리하다.

#### QueryDSL Specification Pattern (권장)

- 서비스에서 where 절 나열 금지
- `BooleanExpression` 반환 정적 메서드로 정의, 조건 조립:

```java
where(MemberSpec.isActive(active), MemberSpec.hasRole(role), ...)
```

### 2.7 패키지 구조 (REST API 전용)

```
apps/{app}-api/src/main/java/com/example/{app}/
└── domain
    └── {app-specific-domain}
        └── api               # 앱 진입점/앱 전용 조합

libs/backend/common/src/main/java/com/example/global/
├── entity                    # BaseTimeEntity
├── payload/response          # RestApiResponse, ApiErrorDetail, IdResponse 등
├── annotation                # CurrentAccount
├── version                   # ApiVersioning
├── utils                     # TraceIdUtils, PaginationUtils 등
└── aop/support               # ControllerLogMessageFactory 등

libs/backend/global-core/src/main/java/com/example/global/
├── config
├── exception
├── security
└── event

libs/backend/domain-core/src/main/java/com/example/domain/
└── {domain}
    ├── api                   # 🚨 /controller 경로 사용 금지
    ├── entity
    ├── enums
    ├── payload
    │   ├── request
    │   ├── response
    │   └── dto
    ├── repository
    ├── service
    │   ├── command
    │   └── query
    ├── validator
    ├── client
    │   └── payload
    ├── config
    └── support
```

#### 도메인별 특수 구조 (AI 참고용)

모든 도메인이 위 표준 레이아웃을 100% 따르지는 않는다.
아래 도메인은 역할 특성상 일부 패키지를 생략하며, 이는 **의도된 설계**이다.

| 도메인          | 특수 구조                                                               | 사유                                                                              |
|--------------|---------------------------------------------------------------------|---------------------------------------------------------------------------------|
| **account**  | `entity`/`repository` 없음                                            | `AccountMemberQueryPort`를 통해 member 도메인에 위임하는 **조회·조합 전용 도메인**                  |
| **security** | `api`/`entity`/`repository` 없음, `port/`·`adapter/` 분리               | Guard·세션 인증 등 **횡단 관심사 도메인**, 자체 영속 엔티티 없음. `port/`=외부 도메인 계약, `adapter/`=포트 구현 |
| **social**   | 루트에 `service` 없음, `google/` 서브도메인 중심                                | 소셜 제공자별 서브도메인 구조(`social/google/service/`), 제공자 추가 시 동일 패턴 복제                   |
| **aws**      | `entity`/`repository`/`validator` 없음                                | S3 파일 업로드 등 **외부 인프라 연동 전용 도메인**                                                |
| **log**      | 이벤트 리스너 + 관리자 조회 API                                                | 활동 로그는 이벤트 리스너로 저장, 관리자 로그 조회용 `api/` 존재                                        |
| **init**     | `service/command/` + `support/`만 존재, `api`/`entity`/`repository` 없음 | 로컬 시드 데이터 오케스트레이터, Port/Adapter로 다른 도메인에 시드 위임                                  |

---

## 3. 아키텍처 규칙 (Architecture Rules)

### 3.1 CQRS (Command / Query 분리) — CRITICAL

- **CommandService**: 생성/수정/삭제, `@Transactional` 필수, 반환 `void` 또는 생성 ID
    - 예외: 인증 토큰 발급/이미지 업로드 URL 반환은 응답 DTO 허용
- **QueryService**: 조회, `@Transactional(readOnly = true)` 필수, DTO Projection 우선
- 패키지 분리: `service/command`, `service/query`
- 클래스명: `XxxCommandService` / `XxxQueryService`
- QueryService ↔ CommandService **상호 호출 금지**

#### @Transactional 전파 정책

- 기본 전파: `REQUIRED` (Spring 기본값) — 명시적으로 `propagation` 속성을 지정하지 않는다
- `REQUIRES_NEW`는 **이벤트 리스너(`@TransactionalEventListener`)에서만** 사용한다
    - 이벤트 리스너는 호출자 트랜잭션과 분리되어야 하므로 독립 트랜잭션이 필요
- 서비스 계층에서 `REQUIRES_NEW` 사용은 금지 — 트랜잭션 분리가 필요하면 이벤트로 전환한다

### 3.2 Hexagonal Architecture (Ports & Adapters) — CRITICAL

- 도메인 계층은 인프라(DB, 외부 API, 프레임워크)에 의존하지 않는다
- **Inbound Adapter**: 외부 요청을 도메인으로 연결 → `api/` 패키지 (Controller)
- **Outbound Port**: 도메인이 외부에 요청하는 인터페이스 → `support/` 패키지의 Port 인터페이스
- **Outbound Adapter**: Port 구현체로 인프라를 연결 → `support/` 패키지의 Adapter 구현체
- **의존 방향**: Adapter → Port ← Domain (**항상 안쪽으로**, 역방향 금지)
- 패키지 매핑:

| 패키지                | 헥사고날 역할                 | 설명                                                          |
|--------------------|-------------------------|-------------------------------------------------------------|
| `api/`             | Inbound Adapter         | Controller, 외부 요청 진입점                                       |
| `service/command/` | Application Service     | 상태 변경 유스케이스                                                 |
| `service/query/`   | Application Service     | 조회 유스케이스                                                    |
| `entity/`          | Domain Model            | 핵심 비즈니스 모델 (Aggregate)                                      |
| `support/`         | Outbound Port + Adapter | 도메인 간 경계, 외부 인프라 추상화 (security 도메인은 `port/`·`adapter/`로 분리) |
| `repository/`      | Outbound Adapter        | 같은 도메인 내 JPA 영속화                                            |
| `client/`          | Outbound Adapter        | 외부 API 연동                                                   |

### 3.3 DDD Bounded Context + Vertical Slicing — CRITICAL

- **Vertical Slicing**: 새 기능 추가 시 기술 계층(controller/service/repository)이 아니라 **도메인 슬라이스 단위**로 자른다
    - 하나의 기능은 `api → service → repository`를 해당 도메인 패키지 안에서 수직으로 완결한다
    - 여러 도메인에 걸치는 기능은 Port/이벤트로 연결하되, 각 도메인의 내부 구조는 독립 유지
- 도메인 간 참조는 **Port 인터페이스**(또는 이벤트/DTO/ID)로만 허용, **Repository·Entity·Service 직접 참조 금지**
- Port 인터페이스는 **사용하는(호출하는) 도메인**의 `support` 패키지에 정의 (security 도메인은 `port/`)
- Port 구현체(Adapter)는 **제공하는(구현하는) 도메인**의 `support` 패키지에 배치 (security 도메인은 `adapter/`)
- JPA 연관관계(`@ManyToOne` 등)로 인해 엔티티 참조가 불가피한 경우, Port 반환 타입에 엔티티를 허용하되 **주석으로 사유를 명시**
- Aggregate 내부 필드에 다른 도메인의 관심사(인증 토큰, 외부 연동 키 등)를 혼합하지 않는다
    - 불가피하게 같은 테이블에 저장해야 하면, 접근은 반드시 **해당 도메인의 Port를 경유**

#### 기존 Port/Adapter 목록 (참고용)

> **주의**: 아래 목록은 참고용이다. 코드가 변경되면 이 목록과 불일치할 수 있으므로, **실제 코드의 `support/` 패키지를 정본(canonical source)으로 삼는다.**

| Port (소비자 support/)                | Adapter (제공자 support/)                    | 방향                 |
|------------------------------------|-------------------------------------------|--------------------|
| `AccountMemberQueryPort`           | `AccountMemberQueryPortAdapter`           | account → member   |
| `AccountMemberCommandPort`         | `AccountMemberCommandPortAdapter`         | account → member   |
| `AccountTokenRevocationPort`       | `AccountTokenRevocationPortAdapter`       | account → security |
| `AccountTokenRefreshPort`          | `AccountTokenRefreshPortAdapter`          | account → security |
| `AccountActivityPublishPort`       | `AccountActivityPublishPortAdapter`       | account → log      |
| `MemberTokenRevocationPort`        | `MemberTokenRevocationPortAdapter`        | member → security  |
| `MemberPermissionCheckPort`        | `MemberPermissionCheckPortAdapter`        | member → security  |
| `MemberActivityPublishPort`        | `MemberActivityPublishPortAdapter`        | member → log       |
| `SecurityMemberTokenPort`          | `SecurityMemberTokenPortAdapter`          | security → member  |
| `SecurityMemberAccessPort`         | `SecurityMemberAccessPortAdapter`         | security → member  |
| `SocialMemberRegistrationPort`     | `SocialMemberRegistrationPortAdapter`     | social → member    |
| `SocialLoginTokenPort`             | `SocialLoginTokenPortAdapter`             | social → security  |
| `SocialActivityPublishPort`        | `SocialActivityPublishPortAdapter`        | social → log       |
| `MemberSocialCleanupPort`          | `MemberSocialCleanupPortAdapter`          | member → social    |
| `InitMemberSeedPort`               | `InitMemberSeedPortAdapter`               | init → member      |
| `LogAuthenticationCheckPort`       | `LogAuthenticationCheckPortAdapter`       | log → security     |
| `SecurityAccountAuthQueryPort`     | `SecurityAccountAuthQueryPortAdapter`     | security → account |
| `SecurityLoginActivityPublishPort` | `SecurityLoginActivityPublishPortAdapter` | security → log     |
| `MemberImageCommandPort`           | `MemberImageCommandPortAdapter`           | aws → member       |
| `MemberImageStoragePort`           | `S3MemberImageStoragePortAdapter`         | member → aws       |

#### Shared Kernel (도메인 간 공유 허용 타입)

아래 타입들은 **여러 Bounded Context에서 공통으로 사용되는 Shared Kernel**으로, 도메인 간 직접 참조를 허용한다.

| 타입                      | 소속 도메인              | 공유 사유                                        |
|-------------------------|---------------------|----------------------------------------------|
| `AccountRole`           | account/enums       | 역할 기반 분기·검증에 전 도메인 필수                        |
| `CurrentAccountDTO`     | account/payload/dto | 인증 컨텍스트 전달에 security·member·log 등 필수         |
| `LogType`               | log/enums           | 활동 로그 발행 Port 파라미터로 account·member·social 사용 |
| `MemberActiveStatus`    | member/enums        | 회원 활성 상태 판별에 account·social 등 필수             |
| `MemberType`            | member/enums        | 회원 유형 분기에 account·social 등 필수                |
| `AccountAuthMemberView` | account/payload/dto | 인증 주체 정보 전달에 security 필수                     |
| `LoginMemberView`       | account/payload/dto | 로그인 회원 뷰 전달에 security 필수                     |
| `MemberUploadDirect`    | member/enums        | 이미지 업로드 경로 분기에 aws 도메인 Port 파라미터로 사용         |

- Shared Kernel 타입은 Port 인터페이스 파라미터/반환 타입에 사용 가능
- Shared Kernel 이외의 타입(Service·Repository·Entity·내부 DTO)은 **반드시 Port 경유**

### 3.4 전략 패턴 (Strategy Pattern) — CRITICAL

권한(AccountRole), 게시판 타입 등 **열거형 값에 따라 동일 인터페이스의 구현이 달라지는 경우** 전략 패턴을 사용한다.

#### 기본 원칙

- if-else/switch 타입 분기 금지
- `{Domain}StrategyFactory`로 구현체 분기
- 미등록 타입은 즉시 예외, 암묵적 기본값 금지

#### 구조

```
인터페이스         MemberCommandService
                    ├── getSupportedRoles(): List<AccountRole>
                    └── 비즈니스 메서드들
                           │
추상 클래스         AbstractMemberCommandService (공통 로직)
                           │
구체 클래스         UserMemberCommandService (역할별 차이점만 오버라이드)
                           │
팩토리             MemberStrategyFactory
                    ├── @PostConstruct → getBeansOfType() + getSupportedRoles()로 EnumMap 구성
                    └── getCommandService(role) / getQueryService(role)
```

#### 구현 규칙

1. **인터페이스**: `getSupportedXxx()` 메서드를 선언하여 구현체가 자신이 담당하는 타입을 반환하게 한다
2. **추상 클래스**: 공통 로직(수정, 비활성화, 검증 등)을 Template Method로 구현한다. 차이점은 abstract/protected 메서드로 위임
3. **구체 클래스**: `getSupportedXxx()`를 오버라이드하여 담당 타입을 반환하고, 차이점만 구현한다
4. **팩토리**: `@PostConstruct`에서 `ApplicationContext.getBeansOfType()`으로 빈을 수집하고, `getSupportedXxx()` 반환값으로 `EnumMap`에
   등록한다
5. **프록시 대응**: `@Transactional` 등으로 JDK Dynamic Proxy가 적용될 수 있으므로 `instanceof` 분기 대신 반드시 인터페이스의 `getSupportedXxx()` 메서드를
   사용한다

#### 새 타입/역할 추가 시 체크리스트

- [ ] 구체 Service 클래스의 `getSupportedXxx()` 반환값에 새 타입 포함 확인
- [ ] 또는 새 구체 클래스를 생성하여 `getSupportedXxx()`에 새 타입 반환
- [ ] StrategyFactory가 `@PostConstruct`에서 자동 수집하므로 팩토리 코드 수정 불필요
- [ ] 앱 기동 테스트로 `StrategyFactory 초기화 완료` 로그에 등록된 서비스 수 확인
- [ ] 미등록 타입 예외 테스트 — 모든 enum 값이 커버되는지 검증

#### 구체 클래스가 하나뿐인 경우

앱이 하나(예: user)라서 구체 클래스가 하나만 존재할 때는 `getSupportedXxx()`에서 해당 enum의 **모든 값**(`AccountRole.values()`)을 반환한다.
향후 앱이 추가되어 역할별 분리가 필요해지면, 새 구체 클래스를 만들고 각각 담당 역할만 반환하도록 변경한다.

#### Template Method + Resolver (권장)

- 흐름 동일 + 일부 정책만 다른 경우 Service 내부 if/else 금지
- 공통 흐름은 추상 클래스, 차이점은 Hook 메서드
- `@Transactional(AOP)` 주의: 공통 흐름 메서드를 `final`로 만들지 않기

### 3.5 예외 계층 정책 (Exception Hierarchy)

- 모든 비즈니스 예외는 `BaseAppException`을 상속한다
- 예외 계층 구조:

```
BaseAppException (추상)
├── GlobalException         — 공통/범용 비즈니스 예외 (ErrorCode 기반)
└── SocialException         — 소셜 로그인 관련 예외
```

- `ErrorCode` enum으로 예외 코드/메시지를 중앙 관리한다
- 새로운 예외 타입 추가 시 반드시 `BaseAppException`을 상속하고, `ExceptionLogTemplates`에 로그 템플릿을 등록한다
- `RuntimeException`을 직접 `throw`하지 않는다 — 항상 `GlobalException.of(ErrorCode.XXX)` 패턴 사용
- **예외: `common` 모듈의 저수준 유틸리티** (`OAuthPkceUtils`, `TokenHashUtils` 등)는 `GlobalException`에 의존할 수 없으므로
  `IllegalStateException`을 허용한다 — `common` 모듈은 `global-core`보다 하위 계층이라 `ErrorCode`를 참조할 수 없기 때문

### 3.6 이벤트 기반 로깅

- ❌ `logRepository.save(...)` 직접 호출 금지
- ✅ `eventPublisher.publishEvent(new MemberActivityEvent(...))`
- 활동 로그: `AFTER_COMMIT`에서만 저장
- 예외/운영 로그: 커밋/롤백 모두 기록, `txStatus` 포함
- `AsyncConfig`는 `AsyncConfigurer`를 구현하고 `getAsyncUncaughtExceptionHandler()`를 등록하여 `@Async` 예외 유실을 방지한다
- **이벤트 객체는 `record`로 정의**하여 불변성을 보장한다
    - 이벤트는 발행 후 수정되어서는 안 되므로, `record`의 자동 불변 특성을 활용한다
    - 컬렉션 필드가 있으면 Compact Constructor에서 `List.copyOf()` 등으로 방어적 복사한다

#### Init Data Seeding (로컬 초기 데이터)

로컬 개발 환경에서 프론트엔드가 API로 일일이 데이터를 생성·수정·조회하지 않아도 되도록, **각 도메인별 초기 시드 데이터를 자동 생성**한다.

##### 원칙

- `@Profile("local")`에서만 동작 — 운영·스테이징 환경에는 절대 적용하지 않는다
- **멱등성 보장**: 이미 존재하면 생성하지 않는다 (`existsBy...` 체크 필수)
- `ApplicationReadyEvent` 시점에 실행 — JPA/DDL 초기화 완료 후 안전하게 실행
- 도메인 간 참조는 **Port/Adapter 패턴**을 사용 — init 도메인이 다른 도메인의 Service·Repository를 직접 참조하지 않는다

##### 구조

```
domain-core/src/main/java/com/example/domain/
├── init/
│   ├── service/command/
│   │   └── InitCommandService.java        # @Profile("local"), ApplicationReadyEvent 리스너 (오케스트레이터)
│   └── support/
│       ├── InitMemberSeedPort.java         # member 시드 포트
│       └── Init{Domain}SeedPort.java       # 새 도메인별 시드 포트 추가
└── {domain}/
    └── support/
        ├── InitMemberSeedPortAdapter.java  # member 시드 어댑터
        └── Init{Domain}SeedPortAdapter.java
```

##### 새 도메인 Init 추가 절차

1. `init/support/`에 `Init{Domain}SeedPort` 인터페이스 정의 (존재 확인 + 시드 메서드)
2. 대상 도메인의 `support/`에 `Init{Domain}SeedPortAdapter` 구현
3. `InitCommandService.initOnApplicationReady()`에 시드 메서드 호출 추가
4. DDD Bounded Context 테이블의 Port/Adapter 목록에 추가
5. 아래 시드 데이터 기준 테이블에 기록

##### 시드 데이터 기준

각 도메인이 시드해야 할 데이터 종류와 수량을 아래에 기록한다. 프론트엔드 페이징 테스트가 가능하도록 충분한 수량을 생성한다.

| 도메인    | 시드 내용                               | 비고            |
|--------|-------------------------------------|---------------|
| member | SUPER_ADMIN 1명, ADMIN 10명, USER 51명 | 페이징 테스트 가능 수량 |

> 새 도메인 추가 시 이 테이블에 시드 내용을 반드시 기록한다.

#### AI 친화적 구조 (AI-Friendly Structure)

- **예측 가능한 네이밍**: 클래스명만으로 역할·계층·도메인을 파악할 수 있어야 한다
    - `{Domain}{역할}{계층}` 패턴: `MemberCommandService`, `SecurityMemberTokenPort`, `MemberSocialCleanupPortAdapter`
- **일관된 패키지 구조**: 모든 도메인이 동일한 패키지 레이아웃(`api/entity/enums/payload/repository/service/support`)을 따른다
- **자기 문서화 코드**: 주석보다 명확한 이름과 작은 메서드로 의도를 표현한다
- **단일 진입점**: 도메인 외부 접점은 Controller(인바운드) + Port(아웃바운드)로 한정한다
- **파일당 하나의 public 타입**: 검색·탐색·수정 범위를 최소화한다
- **작은 클래스, 작은 메서드**: AI가 컨텍스트 윈도우 내에서 전체를 파악할 수 있도록 한다
    - public 클래스 300라인, 메서드 30라인 초과 시 분리 고려

---

## 4. REST API 규칙

### 4.1 API-Version 헤더 규칙 (CRITICAL)

- `/api/**`는 `API-Version` 헤더 **필수**, `/api/health` 및 `/api/social/**`만 예외
- 컨트롤러 매핑은 버전 헤더 기반(`version = "..."`)으로 작성
- URL 버전 세그먼트(`/v1`, `/api/v1`) 사용 금지
- 예외: `/api/social/**` 콜백에 한해 URL 버저닝 허용
- 기본값 `0.0`은 유효하지 않으며, 프론트는 `1.0` 명시 전송 필수
- API 테스트 시 `/api/health`, `/api/social/**` 제외 API는 `API-Version` 헤더 필수

### 4.2 응답 구조 & 멱등성

#### 응답 구조

- 성공: `{ "data": ... }`
- 에러: `{ "code": "...", "message": "...", "requestId": "..." }`
- 필드 검증 에러: `errors` 배열 추가 (권장)
- 응답 바디 불필요 시 `204 No Content`

#### 멱등성 (Idempotency)

- HTTP 메서드별 멱등성 보장:

| 메서드      | 멱등성 | 설계 원칙                                            |
|----------|-----|--------------------------------------------------|
| `GET`    | 필수  | 상태 변경 없이 조회만 수행                                  |
| `PUT`    | 필수  | 동일 요청 반복 시 동일 결과 — 전체 교체(upsert) 시맨틱             |
| `DELETE` | 필수  | 이미 삭제된 리소스에 대해 `204` 반환 (에러 아님)                  |
| `POST`   | 비필수 | 생성 API는 본질적으로 비멱등, 중복 방지가 필요하면 유니크 제약으로 DB 레벨 보장 |
| `PATCH`  | 비필수 | 상대적 변경(`+1` 등)은 비멱등, 절대적 변경(`status=ACTIVE`)은 멱등 |

- **이벤트 리스너**: 동일 이벤트가 중복 발행되어도 부수효과가 한 번만 발생하도록 설계한다 (로그 중복 저장 방지 등)
- **외부 API 연동**: 재시도 시 멱등성이 보장되지 않는 외부 API는 호출 전 상태를 확인하거나, 멱등성 키를 전달한다
- 현재 단일 서버 + RDBMS 환경에서는 트랜잭션 ACID가 기본 보호 — 별도 멱등성 키 인프라는 필요 시 도입한다

### 4.3 컨트롤러 작성 원칙

- `RestApiController`로 응답 생성, 서비스에서 `ResponseEntity` 생성 금지
- 모든 API에 `@Operation(summary=...)` 작성
- Health 제외 모든 API에 `version = ApiVersioning.V1` 등 버전 매핑
- 상태 코드: POST→`201 Created`+Location, PUT/PATCH→`200`/`204`, DELETE→`204`

#### 컨트롤러 네이밍 컨벤션

| 접미사              | 어노테이션             | 역할                   |
|------------------|-------------------|----------------------|
| `*ApiController` | `@RestController` | REST API (JSON 응답)   |
| `*Controller`    | `@Controller`     | Thymeleaf 뷰 (화면 렌더링) |

- REST API 전용 컨트롤러는 반드시 `*ApiController`로 명명한다
- Thymeleaf 화면을 반환하는 컨트롤러는 `*Controller`로 명명한다
- 하나의 컨트롤러에서 REST와 뷰를 혼합하지 않는다

#### 컨트롤러 앱 격리 규칙

현재 `apps/user` 하나로 운영하지만, 모노레포 구조상 새 앱을 추가할 수 있다.
멀티 앱 환경에서는 `@ConditionalOnProperty`로 컨트롤러를 격리한다.

- 각 앱의 `Application` 클래스에서 `setDefaultProperties(Map.of("app.type", "..."))`로 앱 타입을 설정한다
    - `UserApiApplication`: `app.type = user`
- 앱 전용 컨트롤러에는 `@ConditionalOnProperty(name = "app.type", havingValue = "...")`를 추가한다

| 구분          | 대상 컨트롤러                                                                                                      | `@ConditionalOnProperty` |
|-------------|--------------------------------------------------------------------------------------------------------------|--------------------------|
| **user 전용** | `AccountSessionApiController`, `AccountAuthDocsApiController`, `AccountApiController`, `SocialApiController` | `havingValue = "user"`   |
| **공통**      | `RootController`, `HealthRestController`                                                                     | 추가 안 함                   |

- 새 앱 추가 시 전용 컨트롤러에 해당 `havingValue`를 추가하여 격리한다

#### API 설계 원칙

- URL은 **리소스 명사(복수형)** 중심, 동사 금지
- `GET`은 조회 전용, 요청 바디 금지 (예외: `/api/social/**` OAuth 콜백)
- `POST`=생성, `PUT`=전체 갱신(멱등), `PATCH`=부분 갱신, `DELETE`=삭제(멱등)
- 목록 조회: 쿼리 파라미터로 검색/필터/정렬/페이징
- 페이징: `page`는 1부터, `size` 최대치 제한 (`PaginationUtils` 정책)

### 4.4 검증 & 예외 처리 (CRITICAL)

#### 예외 처리

- 전역 예외 처리: `@RestControllerAdvice`로 통일
- 에러 `code`는 **ErrorCode enum** 기준, 문자열 하드코딩 금지
- 검증 메시지는 **한국어**
- BindingResult 사용 지양, 검증 실패는 전역 예외 처리로 일원화

#### InitBinder & ModelAttribute 규칙

- ❌ 공용 이름(`form`/`dto`/`request`) 재사용 금지
- ✅ Request DTO 단위 1:1 매칭
- ✅ `addValidators(...)` 사용 (`setValidator(...)` 금지)
- ✅ 방어적 `supports(...)` 필수
- ❌ 컨트롤러에서 Validator 직접 호출 금지 — `@InitBinder` 등록 + `@Valid`/`@Validated` 자동 검증

#### 검증 책임 분리

| 영역                            | 대상                                       |
|-------------------------------|------------------------------------------|
| Request DTO (Bean Validation) | `@NotBlank`, `@Min`, `@Email` 등 단순 필드 검증 |
| InitBinder Validator          | 교차 필드, 조건부 필수값, 옵션 조합, 트리밍               |
| Service 계층                    | DB/트랜잭션 상태 의존 검증                         |

---

## 5. 보안 규칙

### 권한 통제는 PreAuthorize로만 (CRITICAL)

- **모든 컨트롤러**에 `@PreAuthorize` 필수 — 권한 필요 API는 역할 검증, 공개 API는 `@PreAuthorize("permitAll()")`
- ❌ `@PreAuthorize` 없는 컨트롤러 금지 — 누락인지 의도적 공개인지 구분할 수 없으므로
- ❌ 서비스/컨트롤러 내부 if-else 권한 체크 금지
- 인증 필요 API는 세션 기반 인증을 사용한다 (HttpSession + 쿠키)
- SpEL에서 패키지 의존형 `T(...)` 참조 지양 → `@Component` 메서드 호출로 캡슐화
- 인증/인가 체크는 **`MemberGuard`** `@Component`로 통합
- `SecurityUtils`/`SecurityContextHolder` 직접 호출 금지

### 세션 인증 보안 규칙 (CRITICAL)

- 인증 방식: **HttpSession + 쿠키 기반** (Thymeleaf SSR 환경)
- `SessionCreationPolicy.IF_REQUIRED` — 인증 성공 시 세션 자동 생성
- CSRF 활성화 (Thymeleaf 폼 보호), API 경로(`/api/**`)는 CSRF 제외
- 로그아웃 시 세션 무효화(`invalidateHttpSession`) + 인증 정보 삭제(`clearAuthentication`)
- 소셜 OAuth 리프레시 토큰: **AES-GCM 암호화 저장** (`SocialTokenCrypto`)
- API 보안 기본값: 인증 필요, 공개 API만 allowlist 명시

---

## 6. 운영 규칙

### 6.1 Gradle 의존성 점검 실행 규칙 (CRITICAL)

- `./gradlew` 기반 명령은 **사전 확인 질문 없이 즉시 실행**
- 무질의 실행 허용 화이트리스트:
    - `./gradlew -q dependencies --configuration runtimeClasspath`
    - `./gradlew -q dependencyInsight --dependency <artifact> --configuration runtimeClasspath`
    - `./gradlew -q projects`
    - `./gradlew -q properties`
- 점검 순서: `runtimeClasspath` → `dependencyInsight` → 필요 시 `compileClasspath` 비교
- 샌드박스/권한 차단 시 도구 escalation을 즉시 수행
- 실행 실패 시 `--no-daemon`으로 1회 재시도 후, 계속 차단되면 escalation 수행
- 캐시는 기본 사용, 결과 불일치 의심 시에만 `--refresh-dependencies` 1회 허용
- 결과 보고 시 캐시 기준인지 refresh 기준인지 명시
- 의존성 점검 보고 필수 항목: `요청 명령`, `resolve 버전`, `선택 이유`, `위험도`, `권장 조치`

### 6.2 테스트 코드 작성 규칙 (CRITICAL)

#### 기본 원칙

- **순수 단위 테스트**: Spring Context 로딩 없음 (`@SpringBootTest` 금지)
- **테스트 스택**: JUnit5 + Mockito (`@ExtendWith(MockitoExtension.class)`) + AssertJ (`assertThat`)
- **패턴**: AAA (Arrange-Act-Assert), 메서드당 하나의 동작 검증
- **클래스 접근 제한**: 테스트 클래스는 **package-private** (public 금지)
- **final 규칙**: 테스트 코드에서도 재할당 불필요한 변수는 `final` 선언

#### 테스트 구조

- 테스트 패키지는 대상 클래스의 패키지 경로와 동일하게 유지
- 테스트 클래스명: `{대상클래스명}Test`
- 테스트 메서드명: `{메서드명}_{시나리오}_{기대결과}` (snake_case)

#### Mockito 사용 규칙

- 외부 의존성(Repository, Port 등)만 Mock 처리
- `@Mock` + `@InjectMocks` 조합 사용
- `verify()`로 상호작용 검증, `when().thenReturn()`으로 행위 스텁
- 불필요한 스텁은 작성하지 않는다 (`strictStubs` 정책 준수)

#### 테스트 실행 명령

```bash
# 개별 모듈 테스트
./gradlew :libs:backend:common:test
./gradlew :libs:backend:global-core:test
./gradlew :libs:backend:security-web:test
./gradlew :libs:backend:domain-core:test
./gradlew :libs:backend:web-support:test

# 특정 테스트 클래스
./gradlew :libs:backend:common:test --tests "com.example.global.utils.PaginationUtilsTest"

# 전체 백엔드 테스트
./gradlew test
```

#### 테스트/설정 파일 변경 규칙

- TDD는 선택 전략, 전체 강제 아님
- 코드 변경 시 위험도 기준으로 필요하면 사전 요청 없이 테스트 추가/수정/실행 가능
- 사용자가 명시 요청하지 않는 한 **설정 파일 임의 수정 금지**
- 설정 변경 필요 시 사유/영향 범위를 먼저 설명하고 확인 후 진행

### 6.3 도메인 지침 점검 요청

- 사용자가 "점검"을 요청하면 **개발 철학 13대 원칙**(§1.1)을 기준으로 해당 도메인의 코드/설계를 점검하고 결과를 보고한다.
- 점검 시 각 원칙별로 위반 여부를 확인하며, README 및 본 지침의 세부 규칙도 함께 대조한다.
- 점검 범위가 불명확하면 **추정하지 말고 먼저 질문**한다.
- 점검 결과 보고 형식(필수)
    - `요약`: 준수/위배/보류 여부 한 줄 요약
    - `위배 항목`: 항목별로 **우선순위(높음/중간/낮음)**, 근거 규칙, 위치(파일), 설명, 권장 조치 포함
        - 각 위배 항목에는 **`선택번호`(1부터 순번)** 부여
    - `불확실/질문`: 범위나 의도가 불명확하면 추정하지 말고 질문

| 선택번호 | 우선순위 | 규칙/근거                | 위치                          | 설명             | 권장 조치                              |
|------|------|----------------------|-----------------------------|----------------|------------------------------------|
| 1    | 높음   | 예: API-Version 헤더 규칙 | 예: `SomeApiController.java` | 예: 버전 헤더 매핑 누락 | 예: `version = ApiVersioning.V1` 추가 |

- 후속 조치 옵션 제시(필수):
    1. 높음 우선순위 항목만 우선 조치(권장)
    2. 높음 + 중간 항목까지 조치
    3. 전체 항목 일괄 조치
    4. 조치 없이 점검 결과만 확정

### 6.4 문서 & 설정 관리

#### 커밋 / PR 메시지 규칙 (Conventional Commits)

> 상세 컨벤션은 `docs/CI_STRATEGY.md`의 "커밋 / PR 메시지 컨벤션" 섹션을 참조한다.

- 기본 형식: **`<type>: <변경 요약>`** (scope 생략 가능, 한국어 설명)
- `type` 허용값: `feat`, `fix`, `refactor`, `docs`, `chore`, `test`, `rename`, `style`
- 개별 커밋은 간결하게 작성 (squash merge로 최종 합쳐짐)
- **PR 제목이 squash merge 시 최종 커밋 메시지**가 되므로, PR 제목을 정확하게 작성한다
- PR 본문: `## Summary` + `## Test plan` 형식
- 커밋은 **자율 진행** 가능, 푸시/PR 생성/머지는 **사용자 요청 시에만** 진행한다

#### README 확인

- 작업 전 **`docs/backend/README.md`를 반드시 읽고** 전제/정책을 준수한다
- README와 본 문서/사용자 요청이 **충돌하거나 모호하면 즉시 질문**한다
- 정책/규칙이 변경되면 본 문서와 관련 문서에 함께 반영한다
- 정책/규칙 변경 시 `docs/backend/RULES.md`를 우선 갱신하고, 필요 시 관련 문서(README, docs)를 함께 갱신한다

#### 문서 파일 재확인

- 문서 파일(README, RULES 등)은 수정 전 최신 변경 가능성을 고려해 **반드시 다시 읽고** 수정한다

#### 경로/문서 참조 정합성 (CRITICAL)

- 문서에는 **현재 저장소에 실제 존재하는 경로/파일만** 참조한다.
- 경로를 문서에 추가할 때는 `rg --files` 등으로 존재 여부를 먼저 확인한다.

#### 파일 경로 표기 규칙

- `src/main/resources/**` 하위 파일은 **전체 상대경로를 함께 명시**
- Java 파일은 패키지 선언으로 위치 확인 가능하므로 파일명만 명시 가능
- 앱 전용 코드는 `apps/*-api/**`, 공통 코드는 `libs/backend/**` 경계를 명시

#### 마크다운 테이블 포맷팅 규칙

- `docs/` 하위 마크다운 파일의 테이블은 **IntelliJ 스타일**로 정렬한다
- IntelliJ의 "Reformat Table" 결과와 동일한 형식을 유지해야 IDE 경고가 발생하지 않는다
- 정렬 기준: **문자 수(`len()`)** 기준, 동아시아 표시 폭(display width) 아님
- 형식:
    - 내용 행: `| ` + 내용(최대 폭까지 패딩) + ` |`
    - 구분 행: `|` + `-` × (최대 폭 + 2) + `|`
- 테이블을 수정한 후에는 IntelliJ에서 "Reformat Table" (`Ctrl+Alt+L`)로 정렬을 확인한다

#### 스크립트 보호 규칙

- 현재 저장소에는 전용 `scripts/` 디렉토리가 없다.
- `gradlew`, `gradlew.bat` 외 shell 스크립트 추가/수정은 사용자 요청이 있을 때만 진행한다.

#### 설정파일 관련 의도사항

- 설정파일에 평문이 존재하거나 prod 활성화가 되어 있어도 **의도된 사항**으로 간주
- 보안/권장사항을 이유로 임의 변경 금지 (사용자 명시 요청 시만 예외)

### 6.5 AI 자율 실행 규칙 (CRITICAL)

- 코드 생성/수정/리팩토링/삭제는 **권한 확인 없이 자율 진행**한다
- 빌드·테스트·린트 실행도 자율 진행
- 리팩토링 중 발견된 위반 사항은 즉시 수정한다
- 코드 변경으로 **새로운 패턴·규칙·컨벤션이 확립**되면 본 지침서(`RULES.md`)에 즉시 반영한다
- 코드 변경 완료 후 `RULES.md`와 `docs/backend/README.md`를 확인하여 **현재 코드와 불일치하는 내용이 있으면 함께 수정**한다
- **구조 변경**(모듈 추가/삭제, 파일 이동, 패키지 재구성 등) 시 `docs/backend/README.md`의 구조도·테스트 현황·명령어 등을 반드시 확인하고 불일치하면 즉시 수정한다
- 코드 변경 완료 후 **커밋은 자율 진행**, 푸시/PR은 사용자 명시 요청 시에만 진행

### 6.6 Serena 메모리 관리 규칙

- `.serena/memories/` — **git 공유**: 팀 공통 컨벤션, 프로젝트 구조, 빌드 명령, 체크리스트
- `.serena/cache/` — **로컬 전용**: LSP 캐시 (`.serena/.gitignore`에서 제외)
- 메모리 변경 시 코드 변경과 동일하게 커밋하여 팀 전체에 공유한다
- 개인 설정/로컬 환경 정보는 메모리에 기록하지 않는다

| 메모리 파일                              | 내용                               | 변경 시점      |
|-------------------------------------|----------------------------------|------------|
| `project_overview`                  | 기술 스택, 프로젝트 구조, 도메인, 테스트 현황 (전체) | 구조 변경 시    |
| `backend/style_and_conventions`     | 백엔드 코딩 규칙, CQRS, DDD, 테스트 컨벤션    | 규칙 변경 시    |
| `backend/suggested_commands`        | 백엔드 빌드/테스트/실행 명령 모음              | 명령 추가/변경 시 |
| `backend/task_completion_checklist` | 백엔드 작업 완료 후 점검 항목                | 체크리스트 변경 시 |

> 프론트엔드 메모리는 `frontend/` 토픽 아래에 동일 패턴으로 추가한다.

---

## 7. 품질 체크리스트

코드 작성/수정 완료 후 아래 항목을 점검한다. 각 항목의 상세 규칙은 괄호 안의 섹션을 참조한다.

### 예외/Null/경계값

- [ ] NPE 가능성 없는가?
- [ ] 경계값(0, 음수, null, empty, max length) 처리되는가?

### DTO 규칙 (→ §2.2, §2.3)

- [ ] DTO는 record인가?
- [ ] from/of 정적 팩토리 존재하는가?
- [ ] 외부에서 `new DTO(...)` 호출하지 않는가?
- [ ] Response에 필드 4개 이상이면 의미 단위 DTO로 묶었는가?
- [ ] 기존 DTO를 재사용할 수 있는데 필드를 중복 나열하지 않았는가?

### 아키텍처 (→ §3.1, §3.2, §3.3)

- [ ] 다른 도메인의 Repository를 직접 주입/사용하지 않는가?
- [ ] 다른 도메인의 Service를 직접 호출하지 않고 Port/Event를 경유하는가?
- [ ] Aggregate에 다른 도메인의 관심사가 혼합되어 있지 않은가?
- [ ] 도메인 계층이 인프라(DB, 외부 API)에 직접 의존하지 않는가?
- [ ] 의존 방향이 항상 안쪽(Adapter → Port ← Domain)인가?
- [ ] 도메인 외부 접점이 Controller(인바운드) + Port(아웃바운드)로 한정되는가?

### API & 컨트롤러 (→ §4.1, §4.3)

- [ ] `RestApiController`로 응답 생성하는가?
- [ ] Health 제외 API에 `version = ApiVersioning.*` 명시되는가?
- [ ] `/api/health`, `/api/social/**` 제외 API에 `API-Version` 헤더가 필수로 처리되는가?
- [ ] 멀티 앱 환경 시 앱 전용 컨트롤러에 `@ConditionalOnProperty(name = "app.type", havingValue = "...")` 가 선언되어 있는가?

### 보안 (→ §5)

- [ ] `@PreAuthorize` 누락으로 공개되는 API 없는가?
- [ ] 인증 필요 API에서 세션 미존재 시 적절한 응답(401 또는 리다이렉트)이 반환되는가?
- [ ] 로그아웃 시 세션이 무효화되는가?

### AI 친화적 구조 (→ §3.6 AI 친화적 구조)

- [ ] 클래스명만으로 역할·계층·도메인을 파악할 수 있는가?
- [ ] 모든 도메인이 동일한 패키지 레이아웃을 따르는가?
- [ ] public 클래스 300라인, 메서드 30라인 이내인가?

### 테스트 (→ §6.2)

- [ ] 새로운 유틸리티/서비스/Validator 추가 시 대응하는 단위 테스트가 있는가?
- [ ] 테스트가 Spring Context 없이 순수 단위 테스트로 작성되었는가?
- [ ] `./gradlew test` 전체 통과하는가?

### Enum 계약 동기화

- [ ] API 계약 Enum(`com.example.domain.contract.enums.*`)과 대응 도메인 Enum의 `name()`이 동기화되어 있는가?
- [ ] Enum 변경 시 매핑(`toDomain()` / `fromDomain(...)`) 갱신이 반영되어 있는가?
- [ ] `pnpm nx test domain-core` 동기화 테스트를 통과했는가?
- [ ] `./gradlew :libs:backend:domain-core:generateContractEnumTs` 실행하여 TS 파일을 갱신했는가?

### 기술 스택 (→ §1.4)

- [ ] import가 `tools.jackson.*`인가? (어노테이션은 `com.fasterxml.jackson.annotation.*`)
- [ ] 연관관계 `fetch = LAZY` 명시되는가?
- [ ] 엔티티에 `final` 남용 없는가? (프록시/Dirty Checking 방해 금지)
- [ ] `"\n"` 하드코딩 없는가? Text Block 사용하는가?
- [ ] record/pattern matching/switch expression 우선 사용하는가?
- [ ] `version = ApiVersioning.*` 선언되는가?
- [ ] 신규 `RestTemplate` 도입 피했는가?

### 설정/운영 (→ §6.4)

- [ ] 설정 변경 사유/영향 범위를 먼저 설명하고 확인받았는가?
- [ ] 문서/코드에서 실제 존재하지 않는 경로를 참조하지 않았는가?

---

## 8. 요약 (Cheatsheet)

| 구분            | 규칙                                                                                                   | 상세 섹션 |
|---------------|------------------------------------------------------------------------------------------------------|-------|
| DTO           | record + `from/of`, 외부 `new` 금지                                                                      | §2.2  |
| Response 구조화  | 필드 나열 금지, 의미 단위 DTO로 묶어 조합 (3개 이하 단순 응답은 예외)                                                         | §2.3  |
| 계층 경계         | 값 나열 금지, DTO 1개로 전달                                                                                  | §2.4  |
| 도메인 경계        | Port/Event/DTO/ID로만 참조, Repository·Entity·Service 직접 참조 금지                                           | §3.3  |
| 헥사고날 아키텍처     | 의존 방향 항상 안쪽으로, Port/Adapter로 도메인-인프라 격리                                                              | §3.2  |
| AI 친화적 구조     | 예측 가능한 네이밍(`{Domain}{역할}{계층}`), 일관된 패키지, 자기 문서화 코드                                                   | §3.6  |
| 스크립트          | `gradlew`/`gradlew.bat` 외 shell 스크립트 추가/수정은 사용자 요청 시만 진행                                             | §6.4  |
| CQRS          | 물리 분리, Command=`@Transactional`, Query=`readOnly=true`                                               | §3.1  |
| 조회 최적화        | QueryDSL + fetch join, DTO Projection                                                                | §2.6  |
| 로깅            | traceId 포함, 민감정보 금지                                                                                  | §2.1  |
| API 버전        | `version = ApiVersioning.*`, 기본 `0.0`(무효), `API-Version` 헤더 필수                                       | §4.1  |
| 컨트롤러          | `RestApiController` 응답, 서비스에서 `ResponseEntity` 금지                                                    | §4.3  |
| 컨트롤러 격리       | 멀티 앱 시 앱 전용 컨트롤러에 `@ConditionalOnProperty(name = "app.type")` 추가                                     | §4.3  |
| 설정 변경         | 설정 변경 사유/영향 범위를 먼저 설명하고 확인 후 진행                                                                      | §6.4  |
| 테스트           | 순수 단위 테스트(JUnit5+Mockito+AssertJ), `@SpringBootTest` 금지                                              | §6.2  |
| Enum 계약 동기화   | `Api* == Domain name()` 유지 + 빌드 시 TS 자동 생성(`generateContractEnumTs`) + `pnpm nx test domain-core` 통과 | §7    |
| 외부 연동         | SDK → `@HttpExchange` → `@EnableHttpServices`                                                        | §1.4  |
| 보안            | `@PreAuthorize`만, 누락=공개                                                                              | §5    |
| 인증            | HttpSession + 쿠키 기반, CSRF 활성화 (API 제외)                                                               | §5    |
| JPA           | `LAZY` 명시, `EAGER` 금지                                                                                | §2.6  |
| 멀티라인          | `"\n"` 금지, Text Block 사용                                                                             | §2.1  |
| InitBinder    | DTO 1:1 매칭, 공용 이름 금지, `supports()` 방어                                                                | §4.4  |
| 검증            | Bean Validation + InitBinder Validator, 서비스는 최종 보장만                                                  | §4.4  |
| Java 25       | record/pattern matching/switch 우선 + ScopedValue/Virtual Thread                                       | §1.4  |
| Spring 7      | API Versioning + `@HttpExchange` 우선                                                                  | §1.4  |
| Spring Boot 4 | HTTP Service Clients/Virtual Thread/OpenTelemetry 우선                                                 | §1.4  |
| 버전 하한         | Java 25 + Boot 4 + Framework 7 미만 호환 타협 금지                                                           | §1.4  |
| Jackson 3     | `tools.jackson.*`, 어노테이션만 `com.fasterxml.jackson.annotation.*`                                       | §1.4  |
| Logback       | XML 설정 파일 미사용                                                                                        | §2.1  |
| Serena 메모리    | `.serena/memories/`는 git 공유, `.serena/cache/`는 로컬 전용                                                 | §6.6  |
| 의존성 문서        | `build.gradle.kts` 의존성 변경 시 `docs/backend/BACKEND_DEPENDENCIES.md`도 반드시 함께 갱신                        | §6.4  |
