# UI/UX 디자인 지침

> CRITICAL: Thymeleaf 화면을 생성/수정할 때는 이 지침을 **반드시 읽고 준수**한다.
> AI가 생성한 느낌이 나지 않도록 **일관되고 절제된 디자인**을 유지한다.

---

## 문서 트리거 조건

| 작업 유형              | 읽어야 할 섹션                         |
|--------------------|----------------------------------|
| 새 페이지 생성           | 전체 (특히 "공통 페이지 패턴"에서 해당 패턴 참조)   |
| 기존 페이지 수정          | "디자인 철학" + 해당 컴포넌트 스타일 + "금지 사항" |
| CSS 변경             | "색상 체계" + "타이포그래피" + "간격 체계"     |
| 새 컴포넌트 추가          | "기술 스택" + "컴포넌트 스타일 가이드"         |
| WebJar/라이브러리 버전 변경 | "기술 스택 → WebJars 경로 규칙"          |

---

## 디자인 철학

| 원칙         | 설명                            |
|------------|-------------------------------|
| **일관성**    | 색상·간격·타이포그래피·컴포넌트를 전역에서 통일    |
| **가독성 우선** | 텍스트 크기·행간·명암비를 충분히 확보         |
| **자연스러움**  | 그라디언트·그림자·애니메이션을 과하지 않게 사용    |
| **접근성**    | WCAG 2.1 AA 이상 명암비, 키보드 탐색 가능 |

---

## 기술 스택

| 영역           | 기술                          | 버전             | 용도                                   |
|--------------|-----------------------------|----------------|--------------------------------------|
| CSS/UI 프레임워크 | **Tabler** (Bootstrap 5 기반) | 1.4.0          | UI 컴포넌트, 레이아웃, 그리드                   |
| 인터랙션         | **HTMX**                    | 2.0.7          | JS 없이 서버 인터랙션 (부분 페이지 갱신)            |
| 템플릿 엔진       | **Thymeleaf**               | Spring Boot 내장 | SSR HTML 렌더링                         |
| 레이아웃         | **Thymeleaf 파라미터 Fragment** | —              | `th:fragment` + `th:replace` 기반 레이아웃 |
| 의존성 관리       | **WebJars** (npm)           | —              | 정적 라이브러리를 JAR로 관리                    |

### WebJars 경로 규칙

- WebJars 라이브러리는 Gradle 의존성(`org.webjars.npm:*`)으로 관리한다
- CDN 사용 금지 — 내부망/오프라인 환경 대응을 위해 WebJars로 통일
- Thymeleaf에서 WebJar 파일 참조 시 **버전 포함 경로** 사용:

```html
<!-- Tabler CSS -->
<link th:href="@{/webjars/tabler__core/1.4.0/dist/css/tabler.min.css}" rel="stylesheet">
<link th:href="@{/webjars/tabler__core/1.4.0/dist/css/tabler-vendors.min.css}" rel="stylesheet">

<!-- Tabler JS -->
<script th:src="@{/webjars/tabler__core/1.4.0/dist/js/tabler.min.js}"></script>

<!-- HTMX -->
<script th:src="@{/webjars/htmx.org/2.0.7/dist/htmx.min.js}"></script>
```

- WebJar 버전 업그레이드 시 `build.gradle.kts`와 **모든 레이아웃 템플릿의 경로**를 함께 수정한다

### HTMX 사용 규칙

- **JS 최소화**: 가능한 모든 서버 인터랙션은 HTMX 속성으로 처리
- **Fragment 반환**: HTMX 요청에 대한 컨트롤러 응답은 전체 페이지가 아닌 **Thymeleaf fragment** 반환

```html
<!-- HTMX로 부분 갱신 -->
<button th:attr="hx-get=@{/members/search}" hx-target="#member-list" hx-swap="innerHTML">
    회원 검색
</button>
<div id="member-list"><!-- fragment가 여기에 삽입 --></div>
```

### Tabler 컴포넌트 우선 사용

- Tabler 컴포넌트를 우선 사용, Bootstrap 컴포넌트는 Tabler에 없을 때만 직접 사용
- 커스텀 CSS는 최소화 — Tabler/Bootstrap 유틸리티 클래스 우선 활용

### 보안 (정적 리소스 공개 경로)

- `SecurityPublicPaths.PUBLIC_URLS`에 등록된 경로만 인증 없이 접근 가능
- `/webjars/**`, `/css/**`, `/js/**`, `/images/**` 등록 필수
- 새로운 정적 리소스 경로 추가 시 `SecurityPublicPaths`에 반드시 등록

---

## 색상 체계 (Color Palette)

CSS 변수로 정의하며, **다크 모드**는 추후 확장 시 변수만 덮어쓴다.

```css
:root {
    /* ── 기본 ── */
    --color-bg: #FFFFFF;
    --color-surface: #F9FAFB;
    --color-border: #E5E7EB;

    /* ── 텍스트 ── */
    --color-text-primary: #111827;
    --color-text-secondary: #6B7280;
    --color-text-muted: #9CA3AF;

    /* ── 브랜드 / 액션 ── */
    --color-primary: #2563EB;
    --color-primary-hover: #1D4ED8;
    --color-primary-light: #EFF6FF;

    /* ── 상태 ── */
    --color-success: #059669;
    --color-warning: #D97706;
    --color-danger: #DC2626;
    --color-info: #0284C7;

    /* ── 그림자 ── */
    --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
    --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.07);
    --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
}
```

### 색상 사용 원칙

- 배경: `--color-bg` (페이지), `--color-surface` (카드/섹션)
- 텍스트: 본문 `--color-text-primary`, 보조 `--color-text-secondary`, 비활성 `--color-text-muted`
- 액션 버튼: `--color-primary` 하나만 주력으로 사용, 보조 버튼은 `--color-border` 아웃라인
- 상태 색상: 성공(초록), 경고(주황), 위험(빨강), 정보(파랑) — 배경 틴트로 사용
- **원색 남발 금지**: 한 화면에 강조 색상은 최대 2가지

---

## 타이포그래피

```css
:root {
    --font-sans: 'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

    /* 크기 체계 (rem) */
    --text-xs: 0.75rem; /* 12px */
    --text-sm: 0.875rem; /* 14px */
    --text-base: 1rem; /* 16px */
    --text-lg: 1.125rem; /* 18px */
    --text-xl: 1.25rem; /* 20px */
    --text-2xl: 1.5rem; /* 24px */
    --text-3xl: 1.875rem; /* 30px */

    /* 행간 */
    --leading-tight: 1.25;
    --leading-normal: 1.5;
    --leading-relaxed: 1.75;

    /* 굵기 */
    --font-normal: 400;
    --font-medium: 500;
    --font-semibold: 600;
    --font-bold: 700;
}
```

### 타이포그래피 원칙

- `body` 기본: `--text-base`, `--leading-normal`, `--font-normal`
- 제목: `h1` = `--text-3xl` / `h2` = `--text-2xl` / `h3` = `--text-xl`
- 제목 굵기: `--font-semibold` (bold 아님 — 절제된 느낌)
- 코드/숫자: `--font-mono`
- **밑줄 텍스트 금지** (링크 제외)
- **대문자 변환(uppercase) 지양** — 한국어 UI에서는 의미 없음

---

## 간격 체계 (Spacing)

4px 기반 배수 체계:

```css
:root {
    --space-1: 0.25rem; /*  4px */
    --space-2: 0.5rem; /*  8px */
    --space-3: 0.75rem; /* 12px */
    --space-4: 1rem; /* 16px */
    --space-5: 1.25rem; /* 20px */
    --space-6: 1.5rem; /* 24px */
    --space-8: 2rem; /* 32px */
    --space-10: 2.5rem; /* 40px */
    --space-12: 3rem; /* 48px */
    --space-16: 4rem; /* 64px */
}
```

### 간격 원칙

- 요소 내부 패딩: `--space-4` ~ `--space-6`
- 섹션 간 간격: `--space-8` ~ `--space-12`
- 리스트 아이템 간격: `--space-2` ~ `--space-3`
- **임의 숫자(13px, 17px 등) 금지** — 반드시 변수 사용

---

## 라운드 & 보더

```css
:root {
    --radius-sm: 0.25rem; /*  4px */
    --radius-md: 0.375rem; /*  6px */
    --radius-lg: 0.5rem; /*  8px */
    --radius-xl: 0.75rem; /* 12px */
    --radius-full: 9999px; /* pill */
}
```

- 카드/컨테이너: `--radius-lg`
- 버튼: `--radius-md`
- 인풋: `--radius-md`
- 아바타/뱃지: `--radius-full`
- 보더: `1px solid var(--color-border)`

---

## 컴포넌트 스타일 가이드

### 버튼

| 유형        | 스타일                                                                     |
|-----------|-------------------------------------------------------------------------|
| Primary   | `bg: --color-primary`, 텍스트 흰색, hover 시 `--color-primary-hover`          |
| Secondary | `bg: transparent`, `border: --color-border`, 텍스트 `--color-text-primary` |
| Danger    | `bg: --color-danger`, 텍스트 흰색 — 삭제/탈퇴 등 위험 액션에만 사용                       |
| Ghost     | 배경·보더 없음, 텍스트 `--color-primary` — 부가 액션용                                |

```
공통:
- 높이: 36px (sm) / 40px (md) / 44px (lg)
- 패딩: --space-3 --space-4
- font-weight: --font-medium
- transition: all 0.15s ease
- 비활성(disabled): opacity 0.5, cursor not-allowed
```

### 카드

```
- 배경: --color-bg
- 보더: 1px solid --color-border
- 라운드: --radius-lg
- 그림자: --shadow-sm (hover 시 --shadow-md)
- 패딩: --space-6
```

### 입력 필드 (Input / Select / Textarea)

```
- 높이: 40px (input/select)
- 보더: 1px solid --color-border
- 라운드: --radius-md
- 패딩: --space-2 --space-3
- 포커스: border-color --color-primary, ring 2px --color-primary-light
- 에러: border-color --color-danger
- placeholder: --color-text-muted
```

### 테이블

```
- 헤더: bg --color-surface, font-weight --font-medium, text --color-text-secondary
- 행 구분: border-bottom 1px solid --color-border
- hover: bg --color-surface
- 패딩: --space-3 --space-4
- 텍스트 정렬: 문자 left, 숫자 right
```

### 알림/토스트

```
- 위치: 우측 상단 고정
- 라운드: --radius-lg
- 그림자: --shadow-lg
- 지속: 3~5초 후 자동 사라짐
- 색상: 상태 색상의 연한 배경 + 진한 텍스트
```

---

## 레이아웃

### 페이지 구조

```
┌─────────────────────────────────────┐
│ Header (고정, h: 56px)              │
├──────────┬──────────────────────────┤
│ Sidebar  │ Main Content             │
│ (w:240px)│ (max-w: 1200px, 중앙정렬) │
│          │                          │
├──────────┴──────────────────────────┤
│ Footer (선택)                       │
└─────────────────────────────────────┘
```

- **최대 너비**: 콘텐츠 영역 `1200px`, 넓은 테이블은 `1400px`
- **반응형 브레이크포인트**: `640px` (sm), `768px` (md), `1024px` (lg), `1280px` (xl)
- **모바일 사이드바**: 768px 미만에서 햄버거 메뉴로 전환

---

## 아이콘

- 라이브러리: **Lucide Icons** (가볍고, 일관된 스트로크)
- CDN: `<script src="https://unpkg.com/lucide@latest"></script>`
- 크기: 텍스트와 함께 사용 시 `16px ~ 20px`
- 색상: `currentColor` (텍스트 색상 따름)
- **아이콘 단독 사용 금지** — 반드시 텍스트 레이블 병행 (툴팁 최소)

---

## 애니메이션 & 트랜지션

```css
:root {
    --transition-fast: 0.15s ease;
    --transition-normal: 0.2s ease;
    --transition-slow: 0.3s ease;
}
```

- 버튼 hover/focus: `--transition-fast`
- 모달/드로어 열기: `--transition-normal`
- 페이지 전환: `--transition-slow`
- **과한 애니메이션 금지**: bounce, shake, 3D 회전 등 사용하지 않음
- **로딩**: 심플 스피너 또는 스켈레톤 UI

---

## 공통 페이지 패턴

모든 페이지는 아래 패턴 중 하나를 따른다. 새 페이지를 만들 때 해당 패턴의 구조를 그대로 사용한다.

### 로그인 페이지

```
┌─────────────────────────────────────┐
│           (사이드바 없음)             │
│                                     │
│     ┌───────────────────────┐       │
│     │      로고 / 서비스명     │       │
│     │                       │       │
│     │  ┌─────────────────┐  │       │
│     │  │ 아이디            │  │       │
│     │  └─────────────────┘  │       │
│     │  ┌─────────────────┐  │       │
│     │  │ 비밀번호           │  │       │
│     │  └─────────────────┘  │       │
│     │                       │       │
│     │  [ 로그인 (Primary) ]  │       │
│     │                       │       │
│     └───────────────────────┘       │
│                                     │
└─────────────────────────────────────┘
```

- 중앙 정렬, 카드 폼 (`max-width: 400px`)
- 헤더/사이드바/푸터 없음 — 독립 레이아웃
- 에러 메시지: 폼 상단에 `--color-danger` 배경 틴트 알림
- 비밀번호 표시/숨기기 토글 제공

### 목록 페이지

```
┌─────────────────────────────────────┐
│ Header                              │
├──────────┬──────────────────────────┤
│ Sidebar  │ 페이지 제목      [+ 등록]  │
│          │                          │
│          │ ┌──────────────────────┐ │
│          │ │ 검색/필터 영역        │ │
│          │ └──────────────────────┘ │
│          │                          │
│          │ ┌──────────────────────┐ │
│          │ │ 테이블               │ │
│          │ │ ────────────────── │ │
│          │ │ 행 1                │ │
│          │ │ 행 2                │ │
│          │ │ 행 3                │ │
│          │ └──────────────────────┘ │
│          │                          │
│          │ < 1 2 3 ... 10 >         │
│          │                          │
├──────────┴──────────────────────────┤
│ Footer                              │
└─────────────────────────────────────┘
```

- 상단: **페이지 제목** (h2) + 우측 **주요 액션 버튼** (등록/추가)
- 검색/필터: 카드 안에 인라인 배치, 검색 버튼은 `--color-primary`
- 테이블: 전체 너비, 헤더 고정 스타일, 행 hover 배경
- 빈 상태: 테이블 대신 중앙 정렬 안내 텍스트 (`--color-text-muted`)
- 페이지네이션: 테이블 하단 중앙 정렬
- 행 클릭 → 상세 페이지 이동 (또는 우측 액션 버튼)

### 상세 페이지

```
┌─────────────────────────────────────┐
│ Header                              │
├──────────┬──────────────────────────┤
│ Sidebar  │ ← 목록으로  페이지 제목     │
│          │                          │
│          │ ┌──────────────────────┐ │
│          │ │ 정보 섹션 1           │ │
│          │ │ 라벨: 값              │ │
│          │ │ 라벨: 값              │ │
│          │ └──────────────────────┘ │
│          │                          │
│          │ ┌──────────────────────┐ │
│          │ │ 정보 섹션 2           │ │
│          │ │ 라벨: 값              │ │
│          │ └──────────────────────┘ │
│          │                          │
│          │ [수정]  [삭제(Danger)]    │
│          │                          │
├──────────┴──────────────────────────┤
│ Footer                              │
└─────────────────────────────────────┘
```

- 상단: **뒤로가기 링크** (← 목록으로) + 페이지 제목
- 정보 영역: 카드 단위로 섹션 분리, `라벨: 값` 형태의 2열 그리드
- 라벨: `--color-text-secondary`, `--font-medium`
- 값: `--color-text-primary`
- 하단 액션: 수정(Secondary) + 삭제(Danger) 버튼
- 삭제 시: **확인 모달** 필수 (즉시 삭제 금지)

### 폼 페이지 (등록 / 수정)

```
┌─────────────────────────────────────┐
│ Header                              │
├──────────┬──────────────────────────┤
│ Sidebar  │ ← 뒤로     페이지 제목     │
│          │                          │
│          │ ┌──────────────────────┐ │
│          │ │ 라벨                 │ │
│          │ │ [입력 필드          ] │ │
│          │ │ (유효성 에러 메시지)   │ │
│          │ │                      │ │
│          │ │ 라벨                 │ │
│          │ │ [입력 필드          ] │ │
│          │ │                      │ │
│          │ │ 라벨                 │ │
│          │ │ [텍스트 영역         ] │ │
│          │ └──────────────────────┘ │
│          │                          │
│          │     [취소]  [저장(Primary)]│
│          │                          │
├──────────┴──────────────────────────┤
│ Footer                              │
└─────────────────────────────────────┘
```

- 폼 최대 너비: `640px` (넓은 폼은 `800px`)
- 라벨: 입력 필드 상단에 배치, `--font-medium`
- 필수 항목: 라벨 옆 `*` 표시 (`--color-danger`)
- 유효성 에러: 필드 바로 아래, `--text-sm`, `--color-danger`
- 하단 버튼: 우측 정렬, 취소(Secondary) + 저장(Primary)
- 수정 모드: 제목에 "수정" 표시, 저장 버튼 텍스트 "저장"으로 통일

### 에러 페이지 (403 / 404 / 500)

```
┌─────────────────────────────────────┐
│ Header                              │
├──────────┬──────────────────────────┤
│ Sidebar  │                          │
│          │     ┌────────────┐       │
│          │     │    404     │       │
│          │     │            │       │
│          │     │  안내 메시지  │       │
│          │     │            │       │
│          │     │ [홈으로]    │       │
│          │     └────────────┘       │
│          │                          │
├──────────┴──────────────────────────┤
│ Footer                              │
└─────────────────────────────────────┘
```

- 중앙 정렬, 단순한 구성
- 에러 코드: `--text-3xl`, `--font-bold`, `--color-text-muted`
- 안내 메시지: `--text-lg`, `--color-text-secondary`
- 홈으로 버튼: Primary 버튼
- **일러스트/이모지 사용 금지** — 텍스트만으로 구성

### 확인 모달 (삭제/위험 액션)

```
┌──────────────────────────┐
│ 제목                   ✕ │
│                          │
│ 정말 삭제하시겠습니까?     │
│ 이 작업은 되돌릴 수 없습니다│
│                          │
│       [취소]  [삭제]      │
└──────────────────────────┘
```

- 오버레이: 반투명 배경 (`rgba(0,0,0,0.4)`)
- 모달: 중앙 정렬, `max-width: 480px`, `--radius-xl`
- 제목: `--text-lg`, `--font-semibold`
- 설명: `--color-text-secondary`
- 버튼: 취소(Secondary) + 확인(Danger) — 우측 정렬
- ESC 키 / 오버레이 클릭으로 닫기

### 페이지 공통 규칙

| 항목     | 규칙                                       |
|--------|------------------------------------------|
| 페이지 제목 | `h2`, `--font-semibold` — 모든 페이지에 반드시 존재 |
| 뒤로가기   | 상세/폼 페이지는 좌측 상단에 `← 목록으로` 링크             |
| 로딩 상태  | 콘텐츠 영역에 스켈레톤 UI 또는 중앙 스피너                |
| 빈 상태   | `--color-text-muted` 중앙 텍스트 + 액션 버튼      |
| 성공 피드백 | 저장/삭제 후 목록으로 리다이렉트 + 상단 토스트 알림           |
| 에러 피드백 | 폼: 필드별 인라인 에러 / 서버 에러: 상단 알림 배너          |
| 브레드크럼  | 2depth 이상일 때만 표시 (1depth는 페이지 제목으로 충분)   |

---

## 금지 사항 (AI 느낌 방지)

| 금지                 | 이유             |
|--------------------|----------------|
| 그라디언트 배경 남발        | AI 생성물의 전형적 패턴 |
| 과도한 그림자 중첩         | 비현실적 깊이감       |
| 장식용 원형/블롭 도형       | 랜딩 페이지 느낌      |
| 무의미한 아이콘 나열        | 정보 없는 장식       |
| hero 섹션에 큰 일러스트    | SaaS 랜딩 페이지 느낌 |
| 색상 5개 이상 동시 사용     | 산만한 화면         |
| 문단마다 볼드/이탤릭        | 강조 남발          |
| lorem ipsum 더미 텍스트 | 미완성 느낌         |

---

## 파일 구조 컨벤션

```
src/main/resources/
├── static/
│   ├── css/
│   │   └── app.css          # 전역 스타일 (CSS 변수 정의 포함)
│   ├── js/
│   │   └── app.js           # 전역 스크립트
│   └── images/
├── templates/
│   ├── layout/
│   │   └── default.html     # Thymeleaf 레이아웃 템플릿
│   ├── fragments/
│   │   ├── header.html
│   │   ├── sidebar.html
│   │   └── footer.html
│   ├── index.html
│   └── {도메인}/
│       └── {기능}.html
```

### 네이밍 규칙

- HTML 파일: `kebab-case` (예: `member-list.html`)
- CSS 클래스: `kebab-case` (예: `card-header`, `btn-primary`)
- JavaScript: `camelCase` (변수/함수), `PascalCase` (클래스)
- **BEM 표기법 지양** — 단순 `{컴포넌트}-{요소}` 형태 유지

---

## Thymeleaf 컨벤션

- 레이아웃: 순수 Thymeleaf 파라미터 Fragment (`th:fragment` + `th:replace`) 사용
- fragment 분리: 헤더, 사이드바, 푸터는 반드시 fragment로 분리
- 인라인 스타일 금지: 모든 스타일은 CSS 파일에 작성
- 인라인 스크립트 최소화: 이벤트 바인딩은 JS 파일에서 처리
- `th:text`와 `th:utext` 구분: HTML 이스케이프 필요 시 `th:text`, 마크다운 렌더링 등 `th:utext`
