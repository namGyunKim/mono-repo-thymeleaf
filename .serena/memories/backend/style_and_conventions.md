# 백엔드 코딩 규칙 & 컨벤션

## 핵심 개발 철학 (13대 원칙)
SRP, Clean Code, CQRS, DDD + Vertical Slicing, Hexagonal Architecture,
AI 친화적 구조, Immutability First, Fail Fast, Design by Contract,
Least Privilege, Idempotency, Module Boundary, Response DTO Composition

## 아키텍처

### CQRS
- `service/command/` — `*CommandService` (`@Transactional`, 반환 void 또는 ID)
- `service/query/` — `*QueryService` (`@Transactional(readOnly = true)`, DTO Projection)
- Command ↔ Query 상호 호출 금지

### Hexagonal (Port/Adapter)
- 도메인 간 참조: Port 인터페이스(또는 이벤트/DTO/ID)로만
- Port: 사용하는 도메인의 `support/` (security는 `port/`)
- Adapter: 제공하는 도메인의 `support/` (security는 `adapter/`)
- 의존 방향: Adapter → Port ← Domain (항상 안쪽으로)

### DDD + Vertical Slicing
- 기능 추가는 도메인 슬라이스 단위
- Repository/Entity/Service 직접 참조 금지 → Port 경유
- Shared Kernel: AccountRole, CurrentAccountDTO, LogType 등 허용 타입 정의됨

## 코딩 컨벤션

### 일반
- 언어: 한국어 (주석, 커밋, 답변)
- `final` 기본값 (재할당 불필요한 변수)
- 메서드 정렬: public → protected → private
- 중첩 2단계 이내, Guard Clause 패턴 사용
- public 클래스 300라인, 메서드 30라인 초과 시 분리

### Java 25 문법 우선
- `record`, Pattern Matching, `switch expression`
- Text Block + `formatted(...)` (❌ `"\n"` 금지)
- ScopedValue > ThreadLocal, Virtual Thread 우선

### DTO 규칙 (CRITICAL)
- DTO는 무조건 `record`
- 정적 팩토리 필수: `from(...)` (객체→DTO), `of(...)` (원시값→DTO)
- ❌ 외부 `new DTO(...)` 금지
- Record 컬렉션 필드: Compact Constructor에서 `List.copyOf()` 방어적 복사
- Response 4개+ 필드 → 의미 단위 DTO로 묶어 구성

### Lombok
- ✅ 허용: `@Slf4j`, `@RequiredArgsConstructor`, `@Getter`, `@NoArgsConstructor(access = PROTECTED)`
- ❌ 금지: `@Builder`, `@Setter`, `@Data`

### JPA
- `fetch = FetchType.LAZY` 명시, EAGER 금지
- Dirty Checking 우선, 불필요한 save() 지양
- ❌ `equals()`/`hashCode()` 재정의 금지
- 엔티티 기본 생성자 `protected`, 생성은 정적 팩토리
- Command: 엔티티 조회 / Query: DTO Projection
- 파생 쿼리(조건 1~2개) → @Query(JOIN/복잡) → QueryDSL(동적)

### Jackson 3
- `tools.jackson.*` (핵심), `com.fasterxml.jackson.annotation.*` (어노테이션)
- ❌ `com.fasterxml.jackson.databind.*` 금지

### 보안
- 모든 컨트롤러에 `@PreAuthorize` 필수
- 인증 체크: `MemberGuard` @Component로 통합
- ❌ SecurityUtils/SecurityContextHolder 직접 호출 금지
- 리프레시 토큰: 암호화 저장(AES-GCM), 복호화 검증
- 토큰 폐기 시 액세스+리프레시 양쪽 블랙리스트

### API 규칙
- API-Version 헤더 필수 (`version = ApiVersioning.V1`)
- ❌ URL 버전(`/v1`) 금지 (소셜 콜백 예외)
- 응답: `RestApiController`로 생성, 서비스에서 ResponseEntity 금지
- POST→201+Location, PUT/PATCH→200/204, DELETE→204

### 예외
- 모든 비즈니스 예외: `BaseAppException` 상속
- `GlobalException.of(ErrorCode.XXX)` 패턴
- ❌ `RuntimeException` 직접 throw 금지 (common 모듈 유틸 예외)
- 로그 템플릿: `ExceptionLogTemplates`에서 중앙 관리

### 로깅
- traceId: MDC 패턴 자동 출력 (수동 삽입 금지)
- SLF4J 플레이스홀더 `{}` 강제, 문자열 연결 금지
- 보안 상태 변경 → INFO 로깅 필수
- 이벤트 기반 비동기 로깅 (`publishEvent`)

### 테스트 규칙
- 순수 단위 테스트: JUnit5 + Mockito + AssertJ
- ❌ `@SpringBootTest` 금지
- 클래스: package-private, 메서드명: `{메서드}_{시나리오}_{기대결과}`
- `@Mock` + `@InjectMocks`, strictStubs

### 패키지 구조 (도메인별)
```
{domain}/
├── api/              # Controller (Inbound Adapter)
├── entity/           # Domain Model
├── enums/
├── payload/
│   ├── request/
│   ├── response/
│   └── dto/
├── repository/       # JPA (Outbound Adapter)
├── service/
│   ├── command/
│   └── query/
├── validator/
├── client/payload/   # 외부 API
├── config/
└── support/          # Port + Adapter
```

### 컨트롤러 앱 격리
- user 전용: `@ConditionalOnProperty(name = "app.type", havingValue = "user")`
- admin 전용: `havingValue = "admin"`
- 공통: `@ConditionalOnProperty` 없음
