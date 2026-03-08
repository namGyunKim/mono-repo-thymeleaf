# 프론트엔드 (Thymeleaf) 코딩 규칙

## 기술 스택

- **CSS/UI**: Tabler 1.4.0 (Bootstrap 5 기반) — WebJars로 관리
- **인터랙션**: HTMX 2.0.7 — JS 없이 서버 인터랙션
- **템플릿**: Thymeleaf (SSR) + Fragment 기반 레이아웃
- **의존성**: WebJars (npm) — CDN 사용 금지

## WebJars 경로

```html
<!-- Tabler CSS -->
<link th:href="@{/webjars/tabler__core/1.4.0/dist/css/tabler.min.css}" rel="stylesheet">
<link th:href="@{/webjars/tabler__core/1.4.0/dist/css/tabler-vendors.min.css}" rel="stylesheet">
<!-- Tabler JS -->
<script th:src="@{/webjars/tabler__core/1.4.0/dist/js/tabler.min.js}"></script>
<!-- HTMX -->
<script th:src="@{/webjars/htmx.org/2.0.7/dist/htmx.min.js}"></script>
```

## 디자인 철학

- 미니멀리즘, 일관성, 가독성 우선, 자연스러움, 접근성 (WCAG 2.1 AA)

## 문서 구조 (분할됨)

UI/UX 지침은 5개 파일로 분할 관리한다:

| 문서                                      | 포함 섹션                                   |
|-----------------------------------------|-----------------------------------------|
| `docs/frontend/UI_UX_RULES.md`          | 허브 — 트리거 조건, TOC, §1 디자인 철학, §12 금지 사항  |
| `docs/frontend/DESIGN_TOKENS.md`        | §3 색상, §4 타이포, §5 간격, §6 라운드, §10 애니메이션 |
| `docs/frontend/COMPONENTS.md`           | §7 컴포넌트 스타일, §8 레이아웃, §9 아이콘            |
| `docs/frontend/PAGE_PATTERNS.md`        | §11 공통 페이지 패턴 (로그인, 목록, 상세, 폼, 에러, 모달)  |
| `docs/frontend/TEMPLATE_CONVENTIONS.md` | §2 기술 스택, §13 파일 구조, §14 Thymeleaf 컨벤션  |

## 템플릿 구조 (user 앱)

```
apps/user/src/main/resources/
├── templates/
│   ├── layout/default.html    # 기본 레이아웃
│   └── index.html             # 메인 페이지
├── static/css/                # 커스텀 CSS
└── static/js/                 # 커스텀 JS
```

## 핵심 규칙

- Thymeleaf 화면 생성/수정 시 `docs/frontend/UI_UX_RULES.md` 필독 (허브에서 관련 하위 문서로 이동)
- WebJar 버전 업그레이드 시 `build.gradle.kts` + 모든 레이아웃 템플릿 경로 함께 수정
- 레이아웃: `th:fragment` + `th:replace` 파라미터 Fragment 방식
