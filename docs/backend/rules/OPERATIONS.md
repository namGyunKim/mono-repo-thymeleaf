> 이 문서는 [RULES.md](../RULES.md)에서 분할된 하위 문서입니다. 섹션 번호는 원본과 동일합니다.

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

### 6.2 테스트 코드 작성 규칙 → [TESTING.md](TESTING.md)

> 상세 테스트 규칙(2단계 전략, Testcontainers, Mockito, 실행 명령 등)은 [TESTING.md](TESTING.md)를 참조한다.

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

### 테스트 (→ [§6.2 TESTING.md](TESTING.md))

- [ ] 새로운 유틸리티/서비스/Validator 추가 시 대응하는 단위 테스트가 있는가?
- [ ] 단위 테스트는 Spring Context 없이 순수 테스트인가?
- [ ] 통합 테스트는 `IntegrationTestBase`를 상속하고, Docker가 실행 중인가?
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
