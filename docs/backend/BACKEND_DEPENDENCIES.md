# Backend Dependencies

> Spring Boot 기반 백엔드 의존성 목록 (자동 생성일: 2026-03-04)

## 빌드 환경

| 항목                              | 버전    |
|---------------------------------|-------|
| Gradle Wrapper                  | 9.3.1 |
| Spring Boot                     | 4.0.3 |
| io.spring.dependency-management | 1.1.7 |
| Java Toolchain                  | 25    |

## 모듈 구조

```
common ←(api)── global-core ← domain-core ← security-web ← web-support ← apps
```

| 모듈                          | 역할                                               |
|-----------------------------|--------------------------------------------------|
| `libs/backend/common`       | 순수 공유 (entity, payload, utils, annotation)       |
| `libs/backend/global-core`  | 인프라 (security config, exception, event, logging) |
| `libs/backend/domain-core`  | 도메인 로직 + security (JWT, token, port)             |
| `libs/backend/security-web` | 보안 웹 레이어                                         |
| `libs/backend/web-support`  | 웹 공통 지원 레이어                                      |
| `apps/user`             | 사용자 API 앱 (BootJar)                              |
| `apps/admin`            | 관리자 API 앱 (BootJar)                              |

## 공통 의존성 (루트 subprojects 블록)

모든 모듈이 아래 의존성을 자동 상속한다.

### Spring Boot / Spring Framework

| 의존성                                   | 스코프                 | 설명                                       |
|---------------------------------------|---------------------|------------------------------------------|
| `spring-boot-starter-webmvc`          | implementation      | Spring MVC 웹 서버 (내장 Tomcat)              |
| `spring-boot-starter-validation`      | implementation      | Bean Validation (Jakarta Validation API) |
| `spring-boot-starter-aspectj`         | implementation      | AOP (AspectJ) 지원                         |
| `spring-boot-starter-data-jpa`        | implementation      | Spring Data JPA (Hibernate)              |
| `spring-boot-starter-security`        | implementation      | Spring Security                          |
| `spring-boot-devtools`                | developmentOnly     | 개발 시 자동 재시작                              |
| `spring-boot-configuration-processor` | annotationProcessor | `@ConfigurationProperties` 메타데이터 생성      |
| `spring-boot-starter-test`            | testImplementation  | JUnit5, Mockito, AssertJ, MockMvc 통합     |

### 데이터베이스 / JPA

| 의존성                                                  | 버전          | 스코프                 | 설명                          |
|------------------------------------------------------|-------------|---------------------|-----------------------------|
| `org.postgresql:postgresql`                          | BOM 관리      | runtimeOnly         | PostgreSQL JDBC 드라이버        |
| `io.github.openfeign.querydsl:querydsl-jpa`          | 7.1         | implementation      | QueryDSL JPA (OpenFeign 포크) |
| `io.github.openfeign.querydsl:querydsl-apt`          | 7.1:jakarta | annotationProcessor | QueryDSL Q클래스 코드 생성         |
| `jakarta.annotation:jakarta.annotation-api`          | BOM 관리      | annotationProcessor | Jakarta Annotation API      |
| `jakarta.persistence:jakarta.persistence-api`        | BOM 관리      | annotationProcessor | Jakarta Persistence API     |
| `com.github.gavlyukovskiy:p6spy-spring-boot-starter` | 2.0.0       | implementation      | SQL 쿼리 로깅 (P6Spy)           |

### 보안 (JWT)

| 의존성                         | 버전     | 스코프            | 설명            |
|-----------------------------|--------|----------------|---------------|
| `io.jsonwebtoken:jjwt-api`  | 0.13.0 | implementation | JWT 생성/파싱 API |
| `io.jsonwebtoken:jjwt-impl` | 0.13.0 | runtimeOnly    | JWT 구현체       |
| `io.jsonwebtoken:jjwt-gson` | 0.13.0 | runtimeOnly    | JWT Gson 직렬화  |

### 유틸리티

| 의존성                                                 | 버전     | 스코프                               | 설명                     |
|-----------------------------------------------------|--------|-----------------------------------|------------------------|
| `org.projectlombok:lombok`                          | BOM 관리 | compileOnly + annotationProcessor | 보일러플레이트 코드 생성          |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | 3.0.1  | implementation                    | OpenAPI 3 / Swagger UI |

### AWS

| 의존성                          | 버전      | 스코프            | 설명               |
|------------------------------|---------|----------------|------------------|
| `software.amazon.awssdk:bom` | 2.41.23 | platform (BOM) | AWS SDK v2 버전 관리 |
| `software.amazon.awssdk:s3`  | BOM 관리  | implementation | AWS S3 클라이언트     |

## 모듈별 추가 선언

| 모듈             | 추가 내용                                                                               |
|----------------|-------------------------------------------------------------------------------------|
| `common`       | 없음 (`bootJar` 비활성화, `jar` 활성화)                                                      |
| `global-core`  | `api(project(":libs:backend:common"))` — `java-library` 플러그인, transitive 전파         |
| `domain-core`  | `implementation(global-core)` + `generateContractEnumTs` 코드 생성 태스크                  |
| `security-web` | `implementation(global-core, domain-core)`                                          |
| `web-support`  | `implementation(global-core, domain-core, security-web)`                            |
| `user`     | `implementation(global-core, domain-core, security-web, web-support)` — BootJar 활성화 |
| `admin`    | `implementation(global-core, domain-core, security-web, web-support)` — BootJar 활성화 |

## 참고사항

- **version catalog 미사용** — 모든 버전이 루트 `build.gradle.kts`에 인라인 선언
- **QueryDSL OpenFeign 포크** — Spring Boot 4 / Jakarta EE 호환을 위해 공식 대신 `io.github.openfeign.querydsl` 사용
- **`compileOnly` ← `annotationProcessor` 상속** — Lombok이 컴파일 시 정상 동작하도록 설정
- **`domain-core` 코드 생성** — Java enum → TypeScript enum 자동 변환 (`generateContractEnumTs` 태스크, 빌드 시 자동 실행)
