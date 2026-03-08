# 컴포넌트 스타일 가이드

> ← [UI/UX 디자인 지침](UI_UX_RULES.md)으로 돌아가기

UI 컴포넌트 스펙, 레이아웃 구조, 아이콘 사용 규칙.

---

## §7 컴포넌트 스타일 가이드

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

### 구현 상태 (현재 사용 중인 컴포넌트)

| 상태    | 컴포넌트                                                              | 비고                |
|-------|-------------------------------------------------------------------|-------------------|
| ✅ 구현됨 | Navbar, Cards, Badges, Avatars, Tabs, List Groups, Alerts, Tables | `index.html`에서 사용 |
| ⏳ 미구현 | Forms, Modals, Toasts, Sidebar, Breadcrumbs, Drawers              | 해당 페이지 추가 시 구현    |

> 새 컴포넌트를 추가할 때 이 테이블을 갱신한다.

---

## §8 레이아웃

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

## §9 아이콘

- 방식: **Tabler 인라인 SVG** (Tabler 아이콘 세트 기반)
- 크기: 텍스트와 함께 사용 시 `16px ~ 24px`
- 속성: `stroke="currentColor"`, `fill="none"`, `stroke-width="2"`
- 색상: `currentColor` (텍스트 색상 따름)
- **아이콘 단독 사용 금지** — 반드시 텍스트 레이블 병행 (툴팁 최소)
- CDN 사용 금지 — WebJars 정책과 동일하게 외부 CDN 의존을 피한다

```html
<!-- ✅ Tabler 인라인 SVG -->
<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24"
     viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none"
     stroke-linecap="round" stroke-linejoin="round">
  <path d="..."/>
</svg>
```
