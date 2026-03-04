# 백엔드 작업 완료 후 점검 항목

## 1. 코드 품질
- [ ] SRP/CQRS 관점에서 설계 점검했는가?
- [ ] DTO는 record + from/of 팩토리인가? 외부 `new DTO(...)` 없는가?
- [ ] Response 필드 4개 이상이면 의미 단위 DTO로 묶었는가?
- [ ] `final` 기본값, Guard Clause, 중첩 2단계 이내인가?
- [ ] public 클래스 300라인, 메서드 30라인 이내인가?

## 2. 아키텍처
- [ ] 모듈 의존 방향 위반 없는가? (common←global-core←domain-core←security-web←web-support)
- [ ] 도메인 간 참조가 Port/Event/DTO/ID 경유인가?
- [ ] Hexagonal 의존 방향이 안쪽(Adapter→Port←Domain)인가?

## 3. API
- [ ] Health 제외 API에 `version = ApiVersioning.*` 있는가?
- [ ] `RestApiController`로 응답 생성하는가?
- [ ] 컨트롤러에 `@PreAuthorize` 선언되어 있는가?
- [ ] 앱 전용 컨트롤러에 `@ConditionalOnProperty` 있는가?

## 4. 보안 (세션 기반)
- [ ] 인증 필요 API에서 세션 미존재 시 적절한 응답(401/리다이렉트)이 반환되는가?
- [ ] 로그아웃 시 세션 무효화(invalidateHttpSession) + 인증 정보 삭제인가?
- [ ] CSRF: Thymeleaf 폼은 활성화, API(`/api/**`)는 제외인가?
- [ ] 소셜 OAuth 리프레시 토큰: AES-GCM 암호화 저장인가?
- [ ] 민감정보(password/token/secret) 로그 노출 없는가?

## 5. JPA/DB
- [ ] `fetch = FetchType.LAZY` 명시인가?
- [ ] N+1 문제 없는가? (Fetch Join 확인)
- [ ] Enum 변경 시 DB 제약조건 동기화했는가?

## 6. 테스트
- [ ] 새 유틸/서비스/Validator에 단위 테스트 있는가?
- [ ] `@SpringBootTest` 없이 순수 단위 테스트인가?
- [ ] `./gradlew test` 전체 통과하는가?

## 7. 문법/포맷
- [ ] Jackson 3: `tools.jackson.*` import인가?
- [ ] Text Block 사용, `"\n"` 없는가?
- [ ] Java 25 문법(record/pattern matching/switch) 우선인가?

## 8. Enum 계약 동기화 (해당 시)
- [ ] Api* Enum과 도메인 Enum의 name() 동기화인가?
- [ ] 매핑(toDomain/fromDomain) 갱신했는가?
- [ ] `./gradlew :libs:backend:domain-core:generateContractEnumTs` 실행했는가?

## 9. 문서/경로 정합성
- [ ] 코드 변경 후 RULES.md/README.md와 불일치 없는가?
- [ ] 문서에 실제 존재하지 않는 경로 참조 없는가?
- [ ] 구조 변경 시 README 구조도/테스트 현황/명령어 갱신했는가?

## 10. 커밋
- [ ] Conventional Commits 형식인가? (feat/fix/refactor/docs/chore/test/rename/style)
- [ ] Co-Authored-By 트레일러 없는가?
- [ ] 한국어 메시지인가?
