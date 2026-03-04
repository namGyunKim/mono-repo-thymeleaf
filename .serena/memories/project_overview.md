# 프로젝트 개요

## 프로젝트 정보

- **이름**: mono-repo-thymeleaf
- **목적**: Spring Boot + Thymeleaf 기반 모노레포 웹 애플리케이션 (현재 user 앱, 확장 가능 구조)
- **시스템**: Darwin (macOS)

## 기술 스택

| 영역               | 기술                                    | 버전                                   |
|------------------|---------------------------------------|--------------------------------------|
| 언어               | Java                                  | 25 (Gradle Toolchain)                |
| 프레임워크            | Spring Boot                           | 4.0.3                                |
| Spring Framework | 7.x                                   |                                      |
| 빌드               | Gradle                                | 9.3.1 (Wrapper)                      |
| ORM              | JPA + QueryDSL                        | 7.1 (`io.github.openfeign.querydsl`) |
| 뷰                | Thymeleaf                             | SSR                                  |
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
│   └── user/          # 사용자 앱 (로컬 8081, 프로덕션 8080)
│   └── (확장 가능)      # apps/ 아래에 새 앱 추가 가능
├── libs/backend/
│   ├── common/        # 순수 공유 (entity, payload, utils, annotation, version)
│   ├── global-core/   # 인프라 공통 (security, config, exception, event, logging)
│   ├── domain-core/   # 도메인 로직 + Port/Adapter
│   ├── security-web/  # 인증/인가 웹 계층 어댑터
│   └── web-support/   # MVC/AOP/예외/API-Version 필터
├── build.gradle.kts
├── settings.gradle.kts
└── docs/backend/
```

### 모듈 의존 방향 (단방향, 역방향/순환 금지)

```
common ← global-core ← domain-core ← security-web ← web-support ← apps
```

## 도메인 목록

| 도메인      | 역할          | 특이사항                             |
|----------|-------------|----------------------------------|
| account  | 계정 인증/조합    | entity/repository 없음, member에 위임 |
| member   | 회원 관리       | CRUD + 프로필 + 권한                  |
| security | Guard/세션 인증 | 횡단 관심사, port/adapter 분리          |
| social   | 소셜 로그인      | google/ 서브도메인 구조                 |
| aws      | S3 파일 업로드   | 외부 인프라 연동 전용                     |
| log      | 활동 로그       | 이벤트 리스너 기반, 파티셔닝(member_log)     |
| init     | 로컬 시드 데이터   | @Profile("local") 전용             |

## 테스트 현황

| 모듈           | 테스트 파일 수 | 테스트 메서드 수 |
|--------------|----------|-----------|
| common       | 14       | ~95       |
| global-core  | 10       | ~80       |
| security-web | 2        | ~10       |
| domain-core  | 13       | ~80       |
| web-support  | 0        | 0         |
| **합계**       | **40**   | **~270**  |

## Git 전략

- **브랜치**: feature → PR → CI → Squash Merge → develop → main
- **커밋**: Conventional Commits (한국어), Co-Authored-By 금지
- **직접 push 금지**: develop, main 브랜치
- **PR 생성 후**: auto-merge 설정, 즉시 develop 체크아웃

## 컨트롤러 컨벤션

- `*ApiController`: REST API (JSON 응답)
- `*Controller`: Thymeleaf 뷰 (HTML 응답)
- 앱 격리: `@ConditionalOnProperty(name = "app.type", havingValue = "user")`
