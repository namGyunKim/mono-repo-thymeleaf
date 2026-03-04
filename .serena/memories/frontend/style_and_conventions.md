# 프론트엔드 (Thymeleaf) 코딩 규칙

## 기술 스택
- **CSS/UI**: Tabler 1.3.2 (Bootstrap 5 기반) — WebJars로 관리
- **인터랙션**: HTMX 2.0.6 — JS 없이 서버 인터랙션
- **템플릿**: Thymeleaf (SSR) + Fragment 기반 레이아웃
- **의존성**: WebJars (npm) — CDN 사용 금지

## WebJars 경로
```html
<!-- Tabler CSS -->
<link th:href="@{/webjars/tabler__core/1.3.2/dist/css/tabler.min.css}" rel="stylesheet">
<link th:href="@{/webjars/tabler__core/1.3.2/dist/css/tabler-vendors.min.css}" rel="stylesheet">
<!-- Tabler JS -->
<script th:src="@{/webjars/tabler__core/1.3.2/dist/js/tabler.min.js}"></script>
<!-- HTMX -->
<script th:src="@{/webjars/htmx.org/2.0.6/dist/htmx.min.js}"></script>
```

## 디자인 철학
- 미니멀리즘, 일관성, 가독성 우선, 자연스러움, 접근성 (WCAG 2.1 AA)

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
- Thymeleaf 화면 생성/수정 시 `docs/frontend/UI_UX_RULES.md` 필독
- WebJar 버전 업그레이드 시 `build.gradle.kts` + 모든 레이아웃 템플릿 경로 함께 수정
- 레이아웃: `th:fragment` + `th:replace` 파라미터 Fragment 방식
