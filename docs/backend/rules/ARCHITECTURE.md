> 이 문서는 [RULES.md](../RULES.md)에서 분할된 하위 문서입니다. 섹션 번호는 원본과 동일합니다.

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
| `AccountActivityPublishPort`       | `AccountActivityPublishPortAdapter`       | account → log      |
| `MemberPermissionCheckPort`        | `MemberPermissionCheckPortAdapter`        | member → security  |
| `MemberActivityPublishPort`        | `MemberActivityPublishPortAdapter`        | member → log       |
| `MemberSocialCleanupPort`          | `MemberSocialCleanupPortAdapter`          | member → social    |
| `MemberImageStoragePort`           | `S3MemberImageStoragePortAdapter`         | member → aws       |
| `SecurityAccountAuthQueryPort`     | `SecurityAccountAuthQueryPortAdapter`     | security → account |
| `SecurityMemberAccessPort`         | `SecurityMemberAccessPortAdapter`         | security → member  |
| `SecurityLoginActivityPublishPort` | `SecurityLoginActivityPublishPortAdapter` | security → log     |
| `SocialMemberRegistrationPort`     | `SocialMemberRegistrationPortAdapter`     | social → member    |
| `SocialLoginTokenPort`             | `SocialLoginTokenPortAdapter`             | social → security  |
| `SocialActivityPublishPort`        | `SocialActivityPublishPortAdapter`        | social → log       |
| `InitMemberSeedPort`               | `InitMemberSeedPortAdapter`               | init → member      |
| `LogAuthenticationCheckPort`       | `LogAuthenticationCheckPortAdapter`       | log → security     |
| `MemberImageCommandPort`           | `MemberImageCommandPortAdapter`           | aws → member       |

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

#### ExceptionAdvice 순서 전략

- `CustomExceptionAdvice`에 `@Order(Ordered.HIGHEST_PRECEDENCE)`를 적용하여 비즈니스 예외를 최우선으로 처리한다
- 여러 `@RestControllerAdvice`가 존재할 때, 비즈니스 예외(`BaseAppException`)가 Spring 기본 핸들러에 먼저 잡히지 않도록 보장한다
- 새로운 `@RestControllerAdvice` 추가 시 `@Order` 값을 명시하여 처리 순서를 관리한다

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
