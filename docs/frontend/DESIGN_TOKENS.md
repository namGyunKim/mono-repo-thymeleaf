# 디자인 토큰 (CSS 변수 체계)

> ← [UI/UX 디자인 지침](UI_UX_RULES.md)으로 돌아가기

CSS 변수로 정의하는 색상, 타이포그래피, 간격, 라운드, 애니메이션 체계.

---

## §3 색상 체계 (Color Palette)

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

> **현재 상태**: `app.css` 파일이 아직 생성되지 않았으며, 현재는 **Tabler 유틸리티 클래스**로 스타일링한다.
> 아래 CSS 변수는 커스텀 테마 도입 시 적용할 **목표 설계**이다.

### Tabler 색상 클래스 (현재 사용 중)

Tabler 유틸리티 클래스 기반으로 색상을 적용한다:

| 용도    | 클래스 패턴           | 예시                                          |
|-------|------------------|---------------------------------------------|
| 배경    | `bg-{color}`     | `bg-blue`, `bg-azure`, `bg-teal`            |
| 연한 배경 | `bg-{color}-lt`  | `bg-blue-lt`, `bg-purple-lt`                |
| 텍스트   | `text-{color}`   | `text-blue`, `text-secondary`, `text-muted` |
| 상태    | `status-{color}` | `status-green`                              |

사용 가능 색상: `blue`, `azure`, `teal`, `indigo`, `purple`, `orange`, `cyan`, `green`

### 색상 사용 원칙

- 배경: `--color-bg` (페이지), `--color-surface` (카드/섹션)
- 텍스트: 본문 `--color-text-primary`, 보조 `--color-text-secondary`, 비활성 `--color-text-muted`
- 액션 버튼: `--color-primary` 하나만 주력으로 사용, 보조 버튼은 `--color-border` 아웃라인
- 상태 색상: 성공(초록), 경고(주황), 위험(빨강), 정보(파랑) — 배경 틴트로 사용
- **원색 남발 금지**: 한 화면에 강조 색상은 최대 2가지

---

## §4 타이포그래피

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

> **현재 상태**: Pretendard 웹폰트는 아직 로드하지 않으며, 브라우저 시스템 폰트(`-apple-system` 등)로 폴백된다.
> Pretendard 도입 시 WebJars 또는 self-hosted 방식으로 로드한다 (CDN 금지).

### 타이포그래피 원칙

- `body` 기본: `--text-base`, `--leading-normal`, `--font-normal`
- 제목: `h1` = `--text-3xl` / `h2` = `--text-2xl` / `h3` = `--text-xl`
- 제목 굵기: `--font-semibold` (bold 아님 — 절제된 느낌)
- 코드/숫자: `--font-mono`
- **밑줄 텍스트 금지** (링크 제외)
- **대문자 변환(uppercase) 지양** — 한국어 UI에서는 의미 없음

---

## §5 간격 체계 (Spacing)

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

## §6 라운드 & 보더

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

## §10 애니메이션 & 트랜지션

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
