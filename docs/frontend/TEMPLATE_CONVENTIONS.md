# 템플릿 & 기술 스택 규칙

> ← [UI/UX 디자인 지침](UI_UX_RULES.md)으로 돌아가기

기술 스택, WebJars 경로, HTMX, 파일 구조, Thymeleaf 컨벤션.

---

## §2 기술 스택

| 영역           | 기술                          | 버전             | 용도                                   |
|--------------|-----------------------------|----------------|--------------------------------------|
| CSS/UI 프레임워크 | **Tabler** (Bootstrap 5 기반) | 1.4.0          | UI 컴포넌트, 레이아웃, 그리드                   |
| 인터랙션         | **HTMX**                    | 2.0.7          | JS 없이 서버 인터랙션 (부분 페이지 갱신)            |
| 템플릿 엔진       | **Thymeleaf**               | Spring Boot 내장 | SSR HTML 렌더링                         |
| 레이아웃         | **Thymeleaf 파라미터 Fragment** | —              | `th:fragment` + `th:replace` 기반 레이아웃 |
| 의존성 관리       | **WebJars** (npm)           | —              | 정적 라이브러리를 JAR로 관리                    |

### §2.1 WebJars 경로 규칙

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

### §2.2 HTMX 사용 규칙

> **현재 상태**: `htmx.min.js`는 레이아웃에 로드되어 있으나, 현재 템플릿에서 HTMX 속성을 사용하는 곳은 없다.
> 아래 규칙은 HTMX를 도입하는 시점부터 적용한다.

- **JS 최소화**: 가능한 모든 서버 인터랙션은 HTMX 속성으로 처리
- **Fragment 반환**: HTMX 요청에 대한 컨트롤러 응답은 전체 페이지가 아닌 **Thymeleaf fragment** 반환

```html
<!-- HTMX로 부분 갱신 -->
<button th:attr="hx-get=@{/members/search}" hx-target="#member-list" hx-swap="innerHTML">
    회원 검색
</button>
<div id="member-list"><!-- fragment가 여기에 삽입 --></div>
```

### §2.3 Tabler 컴포넌트 우선 사용

- Tabler 컴포넌트를 우선 사용, Bootstrap 컴포넌트는 Tabler에 없을 때만 직접 사용
- 커스텀 CSS는 최소화 — Tabler/Bootstrap 유틸리티 클래스 우선 활용

### §2.4 보안 (정적 리소스 공개 경로)

- `SecurityPublicPaths.PUBLIC_URLS`에 등록된 경로만 인증 없이 접근 가능
- `/webjars/**`, `/css/**`, `/js/**`, `/images/**` 등록 필수
- 새로운 정적 리소스 경로 추가 시 `SecurityPublicPaths`에 반드시 등록

---

## §13 파일 구조 컨벤션

### 현재 구조

```text
apps/user/src/main/resources/
├── static/                    # (커스텀 CSS/JS/이미지 추가 시 생성)
│   ├── css/                   # 전역 스타일 (CSS 변수 정의 포함)
│   ├── js/                    # 전역 스크립트
│   └── images/
├── templates/
│   ├── layout/
│   │   └── default.html       # Thymeleaf 파라미터 Fragment 레이아웃
│   ├── index.html             # 인덱스 페이지
│   └── {도메인}/               # 도메인별 페이지 (추가 시)
│       └── {기능}.html
```

> `static/` 디렉토리는 커스텀 CSS/JS가 필요할 때 생성한다. 현재는 Tabler/HTMX WebJars만 사용한다.

### 향후 확장 구조 (페이지 증가 시)

```text
templates/
├── layout/
│   └── default.html
├── fragments/                  # 공통 fragment (페이지 3개 이상일 때 분리)
│   ├── header.html
│   ├── sidebar.html
│   └── footer.html
├── index.html
└── {도메인}/
    └── {기능}.html
```

> 현재는 `layout/default.html`에 헤더/푸터가 포함되어 있다. 페이지가 3개 이상으로 늘어나면 `fragments/`로 분리한다.

### 네이밍 규칙

- HTML 파일: `kebab-case` (예: `member-list.html`)
- CSS 클래스: `kebab-case` (예: `card-header`, `btn-primary`)
- JavaScript: `camelCase` (변수/함수), `PascalCase` (클래스)
- **BEM 표기법 지양** — 단순 `{컴포넌트}-{요소}` 형태 유지

---

## §14 Thymeleaf 컨벤션

- 레이아웃: 순수 Thymeleaf 파라미터 Fragment (`th:fragment` + `th:replace`) 사용
- fragment 분리: 헤더, 사이드바, 푸터는 반드시 fragment로 분리
- 인라인 스타일 금지: 모든 스타일은 CSS 파일에 작성
- 인라인 스크립트 최소화: 이벤트 바인딩은 JS 파일에서 처리
- `th:text`와 `th:utext` 구분: HTML 이스케이프 필요 시 `th:text`, 마크다운 렌더링 등 `th:utext`

### 레이아웃 파라미터 Fragment 패턴

레이아웃은 4개 파라미터 Fragment로 구성한다:

```html
<!-- layout/default.html -->
<html th:fragment="layout(title, header, content, scripts)">
  <head><title th:replace="${title}">기본 제목</title></head>
  <body>
    <div th:replace="${header}">헤더 영역</div>
    <div th:replace="${content}">콘텐츠 영역</div>
    <script th:replace="${scripts}">스크립트 영역</script>
  </body>
</html>

<!-- 페이지에서 사용 -->
<html th:replace="~{layout/default :: layout(~{::title}, ~{::header}, ~{::content}, ~{::scripts})}">
  <title th:fragment="title">페이지 제목</title>
  <th:block th:fragment="header">...</th:block>
  <th:block th:fragment="content">...</th:block>
  <th:block th:fragment="scripts">...</th:block>
</html>
```

- 모든 페이지는 `layout/default.html`의 `layout` fragment를 사용한다
- 각 페이지는 `title`, `header`, `content`, `scripts` 4개 fragment를 인라인으로 정의한다
