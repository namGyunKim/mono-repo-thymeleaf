> 이 문서는 [RULES.md](../RULES.md)에서 분할된 하위 문서입니다. 섹션 번호는 원본과 동일합니다.

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

#### Thymeleaf 뷰 컨트롤러 작성 원칙

- `@Controller` + `@PreAuthorize` 필수 (REST API 컨트롤러와 동일)
- 뷰 이름(String)을 반환한다 — `ResponseEntity` 사용 금지
- `Model`에 데이터를 바인딩하여 Thymeleaf에 전달한다
- HTMX 부분 갱신 요청에는 fragment만 반환한다 (전체 페이지 아님)
- REST API 메서드와 같은 클래스에 혼합하지 않는다 (→ §4.3 컨트롤러 네이밍 컨벤션)

```java
@PreAuthorize("permitAll()")
@Controller
public class RootController {

    @GetMapping("/")
    public String index(final Model model) {
        model.addAttribute("message", "서버가 정상 작동 중입니다.");
        return "index";  // templates/index.html
    }
}
```

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

#### 검증 책임 분리 (3단계 파이프라인)

요청 검증은 아래 3단계를 순서대로 통과한다:

| 단계 | 영역                            | 대상                                                    | 실행 시점         |
|----|-------------------------------|-------------------------------------------------------|---------------|
| 1  | Request DTO (Bean Validation) | `@NotBlank`, `@Min`, `@Email` 등 단순 필드 검증              | 바인딩 직후        |
| 2  | InitBinder Validator          | 교차 필드, 조건부 필수값, 정책 규칙, DB 중복 체크                       | `@Valid` 검증 시 |
| 3  | Service Guard Clause          | `requireNonNull`, `requireCurrentAccountFull` 등 사전 조건 | 서비스 메서드 진입부   |

- 1단계 실패 시 2단계의 DB 조회를 스킵하는 **최적화 패턴**을 적용한다 (→ `MemberCreateValidator` 참고)
- 3단계는 `AccountInputValidator` 같은 **정적 유틸리티 Validator**로 Guard Clause를 캡슐화한다

#### Validator 네이밍 컨벤션

InitBinder Validator는 역할에 따라 두 가지 카테고리로 구분한다:

| 카테고리           | 네이밍                       | DB 의존               | 역할             | 예시                                   |
|----------------|---------------------------|---------------------|----------------|--------------------------------------|
| 비즈니스 Validator | `*Validator`              | O (Support/Port 경유) | 중복 체크, 존재 여부 등 | `MemberCreateValidator`              |
| 정책 Validator   | `*RequestPolicyValidator` | X (순수 규칙 검증)        | 역할 조합, 조건부 필수값 | `MemberCreateRequestPolicyValidator` |

```java
// 비즈니스 Validator — DB 조회 필요
@Component
@RequiredArgsConstructor
public class MemberCreateValidator implements Validator {
    private final MemberUniquenessSupport memberUniquenessSupport; // DB 경유
    // 1단계 에러가 없을 때만 DB 조회 실행
}

// 정책 Validator — 순수 규칙 검증
@Component
public class MemberCreateRequestPolicyValidator implements Validator {
    // DB 의존 없이 역할/조합 규칙만 검증
}
```

- 하나의 `@InitBinder`에 여러 Validator를 등록할 수 있다: `binder.addValidators(policyValidator, businessValidator)`
- Validator는 `validator/` 패키지에 배치한다

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

#### MemberGuard 메서드 네이밍 패턴

| 접두사      | 역할       | 예시                                                        |
|----------|----------|-----------------------------------------------------------|
| `is*()`  | 상태 확인    | `isAuthenticated()`, `isSuperAdmin()`                     |
| `has*()` | 권한 보유 확인 | `hasAnyAdminRole()`                                       |
| `can*()` | 행위 가능 여부 | `canAccessMember()`, `canAccessSelf()`, `canManageRole()` |

- `@PreAuthorize` SpEL에서 `@memberGuard.isAuthenticated()`, `@memberGuard.canAccessSelf(#id)` 형태로 호출한다
- Guard 내부의 복잡한 권한 로직은 `private` 헬퍼 메서드로 분리한다

### 세션 인증 보안 규칙 (CRITICAL)

- 인증 방식: **HttpSession + 쿠키 기반** (Thymeleaf SSR 환경)
- `SessionCreationPolicy.IF_REQUIRED` — 인증 성공 시 세션 자동 생성
- CSRF 활성화 (Thymeleaf 폼 보호), API 경로(`/api/**`)는 CSRF 제외
- 로그아웃 시 세션 무효화(`invalidateHttpSession`) + 인증 정보 삭제(`clearAuthentication`)
- 소셜 OAuth 리프레시 토큰: **AES-GCM 암호화 저장** (`SocialTokenCrypto`)
- API 보안 기본값: 인증 필요, 공개 API만 allowlist 명시
