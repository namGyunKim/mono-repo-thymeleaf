# 영역별 개발 지침 (CRITICAL)

각 영역의 코드를 생성/수정할 때는 해당 영역의 지침 문서를 **반드시 읽고 준수**한다.

| 영역                | 지침 문서                                             |
|-------------------|---------------------------------------------------|
| 백엔드 (Spring Boot) | `docs/backend/README.md`, `docs/backend/RULES.md` |

> **백엔드 코드를 생성/수정할 때는 반드시 `docs/backend/RULES.md`를 먼저 읽고 모든 규칙을 준수한다.**
> **코드를 추가하거나 수정할 때는 해당 영역의 지침서를 위반하지 않는지 점검하며 작업한다. 위반 사항이 발견되면 즉시 수정한다.**

---

# 첨부 이미지 확인

사용자가 "첨부한 이미지 확인해줘" 등 이미지 참조를 요청하면, 프로젝트 루트 경로의 `*.png` 파일을 확인한다.

---

# 마크다운 문서 규칙

- `.md` 파일의 테이블 포맷팅은 **IntelliJ Actions on Save (Reformat Code)** 가 자동 처리한다
- CLI(Claude Code 등)에서는 마크다운 테이블 리포맷을 시도하지 않는다 — IntelliJ 포맷터와 결과가 달라질 수 있음
- `.md` 파일을 변경한 후에는 사용자에게 **IntelliJ 정렬 단축키(`Ctrl+Alt+L` / `⌥⌘L`)를 눌러 포맷팅**하라고 안내한다

---

# Git 규칙

## 작업 워크플로우 (CRITICAL)

아래 순서를 반드시 지킨다:

1. **작업 시작 전**: develop 브랜치로 체크아웃 → `git pull` → 최신 상태 확인
2. **feature 브랜치 생성**: 최신 develop에서 분기
3. **코드 작성 / 커밋**
4. **CI 확인**: `gh pr list --state open`으로 진행 중인 PR이 없는지 확인 — **CI 진행 중인 PR이 있으면 CI 완료(머지) 후 다음 단계로 진행**한다
5. **rebase**: `git fetch origin && git rebase origin/develop` — 4단계에서 머지된 PR을 포함하여 **반드시 최신 develop 위에 rebase**한다
6. **PR 생성 후**: auto-merge 설정 → **즉시 develop 브랜치로 체크아웃** (`git checkout develop`)

> 상세 배경과 Branch Protection 설정은 [`docs/CI_STRATEGY.md` → PR 생성 워크플로우](docs/CI_STRATEGY.md) 참조

## 커밋 규칙

- 커밋 메시지에 `Co-Authored-By` 트레일러를 **절대 추가하지 않는다**
- **develop, main 브랜치에 직접 push 금지** — 모든 변경은 반드시 **feature 브랜치 → PR → CI 통과 → Squash Merge** 절차를 따른다 (문서 한 줄 수정도 예외 없음)
- PR 생성 후 반드시 **auto-merge를 설정**한다: `gh pr merge <PR번호> --squash --auto`
- **머지 후 브랜치 자동 삭제** 활성화 — feature/chore 등 작업 브랜치는 머지 시 GitHub이 자동 삭제한다

---

# AI 자율 실행 규칙 (CRITICAL)

## 자율 진행 (사용자 승인 불필요)

- 코드 생성/수정/리팩토링/삭제
- 빌드(`./gradlew build`), 테스트(`./gradlew test`), 린트 실행
- 리팩토링 중 발견된 위반 사항 즉시 수정
- 파일 읽기/탐색/검색
- **Git 커밋** (feature 브랜치에서 커밋까지만 자율 진행)
- **작업 시작 전 develop pull**, **PR 생성 전 open PR 확인**, **PR 생성 후 develop 체크아웃** — 워크플로우 절차는 자율 수행

## 사용자 승인 필요

- `git push` (원격 저장소로 푸시)
- PR 생성 / PR 머지
- **GitHub 설정 변경** (Branch Protection, Secrets, Actions 등) — 변경 시 관련 문서도 최신화한다

> **CRITICAL: "커밋해줘" ≠ "PR 해줘"**
>
> 사용자가 **"커밋해줘"** 라고 하면 **커밋까지만** 수행한다. push/PR은 절대 포함하지 않는다.
> 이전 대화에서 push/PR을 수행한 적이 있더라도 **매 요청마다 독립적으로 판단**한다 — 관성으로 행동하지 않는다.

---

# 권한 설정 (`.claude/settings.local.json`)

`defaultMode: "bypassPermissions"` — 기본 도구(Read, Write, Edit, Glob, Grep)는 자동 허용.
Bash 및 MCP 도구는 `allow` 목록으로 관리한다.

| 카테고리           | 허용 패턴                                                                                                                       |
|----------------|-----------------------------------------------------------------------------------------------------------------------------|
| Git / GitHub   | `Bash(git:*)`, `Bash(gh:*)`                                                                                                 |
| 빌드             | `Bash(./gradlew:*)`                                                                                                          |
| 런타임            | `Bash(python3:*)`, `Bash(node:*)`                                                                                           |
| 파일 조작          | `Bash(mv:*)`, `Bash(cp:*)`, `Bash(rm:*)`, `Bash(mkdir:*)`, `Bash(touch:*)`, `Bash(chmod:*)`, `Bash(ln:*)`                   |
| 검색 / 탐색        | `Bash(find:*)`, `Bash(grep:*)`, `Bash(ls:*)`, `Bash(wc:*)`                                                                  |
| 텍스트 처리         | `Bash(cat:*)`, `Bash(head:*)`, `Bash(tail:*)`, `Bash(sed:*)`, `Bash(awk:*)`, `Bash(sort:*)`, `Bash(uniq:*)`, `Bash(diff:*)` |
| 네트워크 / 압축      | `Bash(curl:*)`, `Bash(tar:*)`, `Bash(unzip:*)`                                                                              |
| 출력             | `Bash(printf:*)`, `Bash(echo:*)`                                                                                            |
| 시스템 도구         | `Bash(brew:*)`, `Bash(uvx:*)`                                                                                               |
| DB 접속          | `Bash(pg_isready:*)`, `Bash(PGPASSWORD=readonly_pass psql:*)`                                                               |
| MCP — Serena   | `mcp__serena__*`                                                                                                            |
| MCP — Context7 | `mcp__context7__*`                                                                                                          |
| MCP — Postgres | `mcp__postgres__query`                                                                                                      |

> 새로운 도구/명령이 필요하면 `allow` 목록에 패턴을 추가한다.

---

# MCP 서버

이 워크스페이스에는 세 개의 MCP 서버를 사용한다.

| 출처                           | 서버       | 도구 접두사             | 용도             |
|------------------------------|----------|---------------------|----------------|
| Project MCPs (`.mcp.json`)   | Postgres | `mcp__postgres__*`  | DB 조회 (읽기 전용)  |
| User MCPs (`~/.claude.json`) | Context7 | `mcp__context7__*`  | 최신 라이브러리 문서 조회 |
| User MCPs (`~/.claude.json`) | Serena   | `mcp__serena__*`    | 시맨틱 코드 분석/편집   |

> **대화 시작 시 MCP 활성화 확인 (CRITICAL)**
>
> 매 대화(세션) 시작 시 아래 순서를 **반드시** 실행한다:
> 1. `mcp__serena__activate_project("mono-repo-thymeleaf")` — Serena 프로젝트 활성화
> 2. `mcp__serena__check_onboarding_performed()` — 온보딩 상태 확인 (미수행 시 `onboarding` 실행)
>
> 이 단계를 건너뛰면 시맨틱 코드 도구를 사용할 수 없다.
