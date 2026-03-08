> 이 문서는 [RULES.md](../RULES.md)에서 분할된 하위 문서입니다. 섹션 번호는 원본과 동일합니다.

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

#### 정적 리소스 캐싱 전략 (`WebConfig`)

`WebConfig.addResourceHandlers()`에서 3종류의 리소스 핸들러를 등록한다.

| 핸들러            | 경로            | 캐시 정책                                                  | 비고                                                          |
|----------------|---------------|--------------------------------------------------------|-------------------------------------------------------------|
| Service Worker | `/sw.js`      | `Cache-Control: no-cache`                              | 브라우저 업데이트 정책상 항상 최신 체크                                      |
| WebJars        | `/webjars/**` | prod: `max-age=365d`, local: `no-store`                | `WebJarsResourceResolver` 존재 시 버전 생략 경로 지원                  |
| Static         | `/**`         | prod: `max-age=365d` + Content Hash, local: `no-store` | `VersionResourceResolver` + `CssLinkResourceTransformer` 적용 |

- **local 프로필**: `resourceChain(false)` — 해시/버전 캐시 비활성화로 개발 중 즉시 반영
- **prod 프로필**: `resourceChain(true)` — Content 기반 해시 버전(`output-{hash}.css`)으로 장기 캐시 + 캐시 무효화

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

#### Soft Delete 패턴 (`_LEAVE_` 타임스탬프 맹글링)

Unique 제약조건이 걸린 컬럼의 논리 삭제 시, 값을 `{원래값}_LEAVE_{yyyyMMddHHmmss}` 형태로 변환하여 Unique 충돌을 회피한다.

```java
// Member.withdraw() 예시
final String nowStr = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
this.loginId = "%s_LEAVE_%s".formatted(this.loginId, nowStr);
this.nickName = "%s_LEAVE_%s".formatted(this.nickName, nowStr);
this.active = MemberActiveStatus.INACTIVE;
```

- 조회 시 `_LEAVE_` 접미사가 있는 데이터는 탈퇴 처리된 것으로 간주한다
- 새로운 Unique 컬럼에 Soft Delete를 적용할 때 이 패턴을 동일하게 따른다

#### JPA 컬렉션 필드 규칙

```java
// ✅ 올바른 선언
@OneToMany(...)
private List<MemberImage> memberImages = new ArrayList<>();

// ❌ final 금지 — Hibernate가 PersistentCollection으로 교체 불가
private final List<MemberImage> memberImages = new ArrayList<>();

// ❌ transient 금지 — JPA 영속 대상에서 제외됨
private transient List<MemberImage> memberImages = new ArrayList<>();
```

- Hibernate는 지연 로딩 시 컬렉션 필드를 `PersistentCollection`으로 **교체(replace)** 한다
- `final`이면 교체가 불가능하여 런타임 오류가 발생한다
- `transient`이면 JPA 매핑 대상에서 제외되어 데이터가 누락된다

#### Entity Serializable 규칙

HttpSession 기반 인증을 사용하므로, 세션에 저장될 수 있는 모든 엔티티는 `Serializable`을 구현한다.

```java
// BaseTimeEntity가 Serializable을 구현하므로, 이를 상속하는 엔티티는 자동으로 Serializable
public abstract class BaseTimeEntity implements Serializable {
    // ...
}

// 엔티티마다 serialVersionUID 선언 필수
public class Member extends BaseTimeEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

- `BaseTimeEntity`가 `Serializable`을 구현하지만, 엔티티 클래스에서도 **명시적으로 `implements Serializable` 선언**한다
- `serialVersionUID`는 반드시 선언한다 (JVM 기본 생성 UID는 클래스 변경 시 역직렬화 실패 원인)
- 복합 키 클래스(`@IdClass`, `@EmbeddedId`)도 반드시 `Serializable` 구현 + `serialVersionUID` 선언

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

#### `support/` 패키지 클래스 분류

`support/` 패키지에는 역할이 다른 세 종류의 클래스가 공존한다. 네이밍으로 역할을 구분한다.

| 분류                 | 네이밍 패턴         | 역할                               | 예시                              |
|--------------------|----------------|----------------------------------|---------------------------------|
| **Port 인터페이스**     | `*Port`        | 도메인 간 계약 (인터페이스)                 | `AccountMemberQueryPort`        |
| **PortAdapter 구현** | `*PortAdapter` | Port 구현체, 다른 도메인의 서비스를 위임 호출     | `AccountMemberQueryPortAdapter` |
| **도메인 내부 Support** | `*Support`     | 도메인 내부 재사용 로직 (Validator 등에서 활용) | `MemberUniquenessSupport`       |
| **인프라 Support**    | `*Support`     | 횡단 관심사 유틸리티 (`web-support` 모듈)   | `ExceptionAdviceSupport`        |

- Port/PortAdapter는 **도메인 간 의존 방향을 제어**하기 위한 패턴이다 (→ §3.3)
- 도메인 내부 `*Support`는 해당 도메인의 `support/` 패키지에 위치하며, 외부 도메인에서 직접 참조하지 않는다

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
