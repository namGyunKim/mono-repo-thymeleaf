# 프로젝트 개요

## 프로젝트 정보

- **이름**: mono-repo-thymeleaf
- **목적**: Spring Boot + Thymeleaf 기반 모노레포 웹 애플리케이션 (user 앱 운영 중, admin 앱은 빈 상태)
- **시스템**: Darwin (macOS)

## 기술 스택

| 영역               | 기술                                    | 버전                                   |
|------------------|---------------------------------------|--------------------------------------|
| 언어               | Java                                  | 25 (Gradle Toolchain)                |
| 프레임워크            | Spring Boot                           | 4.0.3                                |
| Spring Framework | 7.x                                   |                                      |
| 빌드               | Gradle                                | 9.3.1 (Wrapper)                      |
| ORM              | JPA + QueryDSL                        | 7.1 (`io.github.openfeign.querydsl`) |
| 뷰                | Thymeleaf + Tabler 1.4.0 + HTMX 2.0.7 | SSR (WebJars, Fragment 레이아웃)         |
| DB               | PostgreSQL                            |                                      |
| 인증               | HttpSession + Spring Security (세션 기반) |                                      |
| JSON             | Jackson 3 (`tools.jackson.*`)         |                                      |
| 파일 저장            | AWS S3                                |                                      |
| 로깅               | SLF4J + P6Spy (SQL)                   |                                      |
| 비동기              | Virtual Thread (JEP)                  |                                      |

## 프로젝트 구조

```
mono-repo-thymeleaf/
├── apps/
│   ├── user/          # 사용자 앱 (로컬 8081, 프로덕션 8080)
│   └── admin/         # 관리자 앱 (로컬 8082, settings.gradle.kts에 미포함)
├── libs/backend/
│   ├── common/        # 순수 공유 (entity, payload, utils, annotation, version, aop)
│   ├── global-core/   # 인프라 공통 (security, config, exception, event, logging, api)
│   ├── domain-core/   # 도메인 로직 + Port/Adapter
│   ├── security-web/  # 인증/인가 웹 계층 어댑터 (config, security)
│   └── web-support/   # MVC/AOP/예외/이벤트/리졸버 (config, aop, exception, event, resolver)
├── infra/docker/      # Dockerfile
├── .github/workflows/ # CI/CD (ci.yml, backend-cd.yml, deploy-user.yml, stage-user.yml)
├── docs/
│   ├── backend/       # RULES.md (§1-§8), README.md, BACKEND_DEPENDENCIES.md, 배포 가이드
│   ├── frontend/      # UI_UX_RULES.md (허브) + 4개 하위 문서
│   └── CI_STRATEGY.md # CI/CD, 브랜치 전략, Branch Protection
├── CLAUDE.md           # AI 행동 규칙 정본 + 문서 정본 체계
├── AGENTS.md           # CLAUDE.md 동기화 미러
├── build.gradle.kts
└── settings.gradle.kts
```

### 모듈 의존 방향 (단방향, 역방향/순환 금지)

```
common ← global-core ← domain-core ← security-web ← web-support ← apps
```

## 도메인 목록

| 도메인      | 역할          | 특이사항                               |
|----------|-------------|------------------------------------|
| account  | 계정 인증/조합    | entity/repository 없음, member에 위임   |
| member   | 회원 관리       | CRUD + 프로필 + 권한                    |
| security | Guard/세션 인증 | 횡단 관심사, port/adapter 분리            |
| social   | 소셜 로그인      | google/ 서브도메인 구조                   |
| aws      | S3 파일 업로드   | 외부 인프라 연동 전용                       |
| log      | 활동 로그       | 이벤트 리스너 기반, 파티셔닝(member_log)       |
| init     | 로컬 시드 데이터   | @Profile("local") 전용               |
| contract | Enum 계약     | TS 코드 생성용 (generateContractEnumTs) |
| config   | 도메인 공통 설정   | 도메인 모듈 내부 Bean 설정                  |

## 코드 규모

- 메인 소스: libs 319파일 + apps 2파일 = 321 Java 파일
- 테스트 소스: 46 Java 파일

## 테스트 현황

| 모듈           | 테스트 파일 수 | @Test 메서드 수 |
|--------------|----------|-------------|
| common       | 14       | 123         |
| global-core  | 6        | 43          |
| security-web | 6        | 41          |
| domain-core  | 18       | 82          |
| web-support  | 2        | 11          |
| **합계**       | **46**   | **300**     |

## Git 전략

- **브랜치**: `feat/*` → PR → CI → Squash Merge → `develop` (기본 브랜치)
- **main 브랜치**: 예약 상태 — 실서버 운영 시 프로덕션 용도로 도입 예정
- **배포**: `deploy/user` 브랜치에 push 시 자동 배포
- **커밋**: Conventional Commits (한국어), Co-Authored-By 금지
- **직접 push 금지**: develop, main 브랜치
- **PR 생성 후**: auto-merge 설정, 즉시 develop 체크아웃

## CI/CD

| 워크플로우             | 트리거                       | 주요 동작                        |
|-------------------|---------------------------|------------------------------|
| `ci.yml`          | PR → develop (md/docs 제외) | `./gradlew :apps:user:build` |
| `backend-cd.yml`  | —                         | 백엔드 CD                       |
| `deploy-user.yml` | —                         | user 앱 배포                    |
| `stage-user.yml`  | —                         | user 앱 스테이징                  |

- CI Java 버전: 21 (Temurin) — 빌드 호환용, 프로젝트 Toolchain은 Java 25
- CI는 `.md`, `.serena/**` 등 문서/설정만 변경된 PR은 job 내부에서 감지하여 빌드 스킵 (job 자체는 성공 보고)

## 문서 정본 체계

| 주제                         | 정본                             | 충돌 시                           |
|----------------------------|--------------------------------|--------------------------------|
| AI 행동 규칙, Git 워크플로우, 권한 설정 | `CLAUDE.md`                    | → `AGENTS.md` 동기화              |
| 백엔드 코딩 규칙, 아키텍처, API 규칙    | `docs/backend/RULES.md`        | → `README.md` 갱신               |
| 프론트엔드 UI/UX                | `docs/frontend/UI_UX_RULES.md` | —                              |
| CI/CD, 브랜치 전략              | `docs/CI_STRATEGY.md`          | → `CLAUDE.md` 갱신               |
| 백엔드 의존성 목록                 | `build.gradle.kts` (코드)        | → `BACKEND_DEPENDENCIES.md` 갱신 |
| Port/Adapter 목록            | 코드 (`support/` 패키지)            | → `RULES.md` 갱신                |

> 코드와 문서 충돌 시 **코드가 정본**. 단, 코드가 규칙 위반이면 코드를 수정.

## 컨트롤러 컨벤션

- `*ApiController`: REST API (JSON 응답)
- `*Controller`: Thymeleaf 뷰 (HTML 응답)
- 앱 격리: `@ConditionalOnProperty(name = "app.type", havingValue = "user")`
