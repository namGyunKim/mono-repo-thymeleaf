# 백엔드 빌드/테스트/실행 명령

## 빌드
```bash
./gradlew :apps:user:build
./gradlew :apps:admin:build
```

## 실행
```bash
./gradlew :apps:user:bootRun    # localhost:8081
./gradlew :apps:admin:bootRun   # localhost:8082
```

## 테스트
```bash
# 전체 테스트
./gradlew test

# 개별 모듈 테스트
./gradlew :libs:backend:common:test
./gradlew :libs:backend:global-core:test
./gradlew :libs:backend:security-web:test
./gradlew :libs:backend:domain-core:test
./gradlew :libs:backend:web-support:test

# 특정 테스트 클래스
./gradlew :libs:backend:common:test --tests "com.example.global.utils.PaginationUtilsTest"

# 전체 라이브러리 테스트 (한 줄)
./gradlew :libs:backend:common:test :libs:backend:global-core:test :libs:backend:security-web:test :libs:backend:domain-core:test :libs:backend:web-support:test
```

## 컴파일 검증 (개별 라이브러리)
```bash
./gradlew :libs:backend:common:compileJava
./gradlew :libs:backend:global-core:compileJava
./gradlew :libs:backend:domain-core:compileJava
./gradlew :libs:backend:security-web:compileJava
./gradlew :libs:backend:web-support:compileJava
```

## 의존성 점검
```bash
./gradlew -q dependencies --configuration runtimeClasspath
./gradlew -q dependencyInsight --dependency <artifact> --configuration runtimeClasspath
./gradlew -q projects
./gradlew -q properties
```

## Enum 계약 TS 생성
```bash
./gradlew :libs:backend:domain-core:generateContractEnumTs
```

## Git 워크플로우
```bash
# 작업 시작
git checkout develop && git pull

# feature 브랜치 생성
git checkout -b feature/기능명

# PR 전 rebase
git fetch origin && git rebase origin/develop

# PR 생성 후
gh pr merge <PR번호> --squash --auto
git checkout develop
```

## 헬스체크 URL
- user: GET http://localhost:8081/api/health
- admin: GET http://localhost:8082/api/health
