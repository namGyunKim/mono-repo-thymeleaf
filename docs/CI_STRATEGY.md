# CI/CD 전략

## 브랜치 전략

```
feat/*  ──→  develop  ──→  deploy/*
          (Squash PR)    (merge/push)
```

| 브랜치           | 역할                        | 보호 규칙                |
|---------------|---------------------------|----------------------|
| `feat/*`      | 기능 개발                     | 없음                   |
| `develop`     | 통합 브랜치 — 모든 feat이 여기로 머지됨 | Branch Protection 적용 |
| `deploy/user` | user 배포 트리거               | push 시 자동 배포         |

> 새 앱 추가 시 `deploy/{app-name}` 브랜치를 생성하여 동일한 배포 패턴을 적용한다.

### main 브랜치 (예약)

현재 `main` 브랜치는 존재하지 않으며, `develop`이 기본 브랜치 역할을 한다.
실서버 운영이 시작되어 테스트/프로덕션 환경 분리가 필요해지면 `main`을 도입한다.

### 브랜치 네이밍 컨벤션

```
feat/be-xxx    # 백엔드 기능
feat/xxx       # 공통/인프라/문서
hotfix/xxx     # 실서버 긴급 수정
```

### 커밋 / PR 메시지 컨벤션

Squash merge를 사용하므로 **PR 제목 = develop에 남는 최종 커밋 메시지**이다.

**PR 제목** (= squash merge 커밋 제목):

```
<type>: <변경 요약>
```

| type       | 용도                   |
|------------|----------------------|
| `feat`     | 새로운 기능 추가            |
| `fix`      | 버그 수정                |
| `docs`     | 문서 변경                |
| `refactor` | 동작 변경 없는 코드 구조 개선    |
| `chore`    | 빌드/설정/CI 등 기능 외 변경   |
| `test`     | 테스트 추가/수정            |
| `rename`   | 이름 변경 (파일, 심볼 등)     |
| `style`    | 포맷팅, 세미콜론 등 코드 의미 무관 |

**PR 본문** (= squash merge 커밋 본문):

```markdown
## Summary

- 변경 사항 1 (무엇을 왜)
- 변경 사항 2

## Test plan

- [ ] 검증 항목 1
- [ ] 검증 항목 2
```

### PR 생성 워크플로우

> 이 워크플로우는 [`CLAUDE.md` → 작업 워크플로우](../CLAUDE.md)와 동일한 내용이다. 한쪽을 수정하면 **반드시 양쪽을 동기화**한다.

```
1. develop checkout → git pull (최신화)
2. feature 브랜치 생성 (최신 develop에서 분기)
3. 코드 작성 / 커밋
4. CI 확인: gh pr list --state open
   └── 진행 중인 PR이 있으면 CI 완료(머지)를 기다린다
5. rebase: git fetch origin && git rebase origin/develop
   └── 4단계에서 머지된 PR을 포함하여 최신 develop 위에 rebase
6. push → PR 생성 → auto-merge 설정
7. 즉시 develop 브랜치로 체크아웃
```

---

## CI 파이프라인

### 트리거 조건

```yaml
on:
  pull_request:
    branches: [ develop ]
  workflow_dispatch:
```

- PR은 항상 CI 워크플로우를 트리거한다 (Branch Protection required check와의 충돌 방지).
- 문서(`.md`)나 `.serena/**`만 변경된 PR은 job 내부에서 감지하여 빌드를 스킵하되, **job 자체는 성공으로 보고**한다.

### Job 구조

```
feat/* → develop PR 생성 시:

┌────────────────────┐
│  backend (~40s)    │
│  Gradle build      │
└────────────────────┘
         ▼
   ✅ 통과 시
  Squash Merge 가능
```

### backend

백엔드 컴파일 및 단위 테스트를 실행한다.

| 항목     | 값                                           |
|--------|---------------------------------------------|
| Runner | `ubuntu-latest`                             |
| Java   | Temurin 21 (Gradle toolchain이 25로 자동 프로비저닝) |
| 명령어    | `./gradlew :apps:user:build`                |
| 포함 범위  | 컴파일, 단위 테스트, QueryDSL 코드 생성                 |
| 평균 소요  | ~40초                                        |

---

## Branch Protection 설정 (develop)

| 옵션                                    | 현재 설정  | 설명                       |
|---------------------------------------|--------|--------------------------|
| **Require a pull request**            | ✅      | 직접 push 차단, PR 필수        |
| **Require approvals**                 | ✅ (0명) | 리뷰 승인 없이 머지 가능           |
| **Require status checks to pass**     | ✅      | CI 통과 필수                 |
| **Required checks**                   | ✅      | `backend` 1개 job 통과 필수   |
| **Require branches to be up to date** | ✅      | 최신 develop 기반으로 CI 통과 보장 |
| **Require linear history**            | ✅      | Squash merge 강제          |
| **Allow auto-merge**                  | ✅      | CI 통과 시 자동 머지            |
| **Delete head branches**              | ✅      | PR 머지 후 작업 브랜치 자동 삭제     |

### CLI로 설정 재현

```bash
# 1. Repository 설정
gh api repos/{owner}/{repo} -X PATCH -f allow_auto_merge=true -f delete_branch_on_merge=true

# 2. Branch Protection
gh api repos/{owner}/{repo}/branches/develop/protection -X PUT \
  --input - <<'EOF'
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["backend"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "required_approving_review_count": 0
  },
  "restrictions": null,
  "required_linear_history": true
}
EOF
```

---

## 배포 전략

배포는 CI와 분리되어 있다. 배포 전용 브랜치에 push하면 해당 프로젝트가 자동 배포된다.

### 배포 흐름

```
개발자: git checkout deploy/user
        git merge develop
        git push
            │
            ▼
GitHub Actions: deploy-user.yml 트리거
    ├── ./gradlew :apps:user:bootJar
    ├── docker build → ghcr.io/.../user:<commit-sha>
    ├── docker push (GHCR)
    └── ssh → /opt/deploy/deploy.sh user <commit-sha>
```

---

## 로컬 개발 워크플로우

```bash
./gradlew test                                # 전체 테스트
./gradlew :apps:user:bootRun              # user 실행
```
