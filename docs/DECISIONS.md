# 기술 결정 기록 (DECISIONS)

---

## ADR-001: ClubRequest payload를 JSON 문자열로 저장

**날짜**: 2026-02-21

**배경/맥락**:
동아리 등록 신청 시 Club 생성에 필요한 모든 정보를 저장해야 한다.
신청 시점과 승인 시점 사이에 Club 스키마가 변경될 수 있으며,
신청 데이터는 이력 보존 목적으로 불변 상태로 관리해야 한다.

**선택지**:
- A. `ClubRequest` 테이블에 Club 필드를 컬럼으로 직접 추가
- B. 별도 `ClubRequestDetail` 테이블로 1:1 분리
- C. `payload` 컬럼에 JSON 문자열로 직렬화하여 저장 ← 선택

**결정**: C — JSON payload 방식 채택

**결과/영향**:
- Club 스키마 변경에 독립적으로 신청 데이터 보존 가능
- `ObjectMapper`로 직렬화/역직렬화; 런타임 오류 가능성 존재
- 승인 시 `deserializePayload()` 호출로 Club 엔티티 재구성
- JSON 내부 필드에 대한 DB 레벨 제약 적용 불가

**후속 작업**:
- payload 최대 크기 제한 검토 (ERD 권고: 10KB)
- `ClubRequestPayload` 역직렬화 실패 시 의미 있는 에러 응답 반환

---

## ADR-002: 상태 전이를 도메인 메서드로 캡슐화

**날짜**: 2026-02-21

**배경/맥락**:
`ClubRequest`의 상태를 `PENDING → APPROVED/REJECTED`로 변경할 때,
변경에 필요한 필드(status, reviewedBy, reviewedAt, adminMemo)가
항상 함께 변경되어야 한다는 규칙을 어디에 위치시킬지 결정이 필요했다.

**선택지**:
- A. 서비스에서 직접 각 필드에 setter 호출
- B. 엔티티에 public setter 추가
- C. 엔티티에 의도가 명확한 도메인 메서드(`approve`, `reject`) 추가 ← 선택

**결정**: C — 도메인 메서드 방식

**결과/영향**:
- 상태 전이 규칙이 엔티티 내부에 응집됨 (Anemic Domain Model 방지)
- `approve(User admin)` / `reject(User admin, String memo)` 시그니처가 비즈니스 의도를 명시적으로 표현
- 도메인 메서드만 단독으로 테스트 가능

**후속 작업**: 없음

---

## ADR-003: 승인 시점에 slug 중복 재검증

**날짜**: 2026-02-21

**배경/맥락**:
신청 생성 시 slug 중복을 검증하지만, 신청~승인 사이에
동일 slug를 가진 다른 동아리가 직접 생성될 수 있다.
승인 후 `UNIQUE` 제약 위반이 발생하면 DB 예외로 처리되어 사용자 경험이 나빠진다.

**선택지**:
- A. 재검증 없이 DB 제약 예외를 그대로 노출
- B. 승인 시점에 slug 중복을 애플리케이션 레벨에서 재검증 ← 선택
- C. 신청 생성 시 slug를 "예약"하여 다른 동아리가 사용하지 못하게 함

**결정**: B — 승인 시 재검증

**결과/영향**:
- 관리자가 의미 있는 에러 메시지(`DUPLICATE_RESOURCE`)를 받음
- 요청이 많지 않은 현재 규모에서 충분한 방어
- 옵션 C는 별도 예약 테이블/락이 필요하여 복잡도 증가

**후속 작업**: 없음

---

## ADR-004: 관리자 목록 조회 시 @EntityGraph로 N+1 방지

**날짜**: 2026-02-21

**배경/맥락**:
`GET /club-requests` 목록 조회 시 각 `ClubRequest`의 `requestedBy`, `reviewedBy`
사용자 정보가 응답에 포함된다. 기본 지연 로딩 시 N+1 쿼리가 발생한다.

**선택지**:
- A. 지연 로딩 그대로 사용 (N+1 허용)
- B. `@EntityGraph(attributePaths = {"requestedBy", "reviewedBy"})` 사용 ← 선택
- C. JPQL JOIN FETCH 커스텀 쿼리 작성

**결정**: B — `@EntityGraph` 사용

**결과/영향**:
- 목록 조회 쿼리 1회로 연관 사용자 정보까지 한 번에 조회
- JPQL 대비 선언적이고 간결
- 복잡한 동적 조건 없는 현재 요건에 적합

**후속 작업**:
- 관리자 목록에 필터링(status별) 또는 페이지네이션 추가 시 재검토

---

## ADR-005: 승인/거절 엔드포인트에 PATCH 사용

**날짜**: 2026-02-21

**배경/맥락**:
`ClubRequest`의 status 필드만 변경하는 부분 업데이트 작업에 적절한 HTTP 메서드 선택이 필요하다.

**선택지**:
- A. `POST /club-requests/{publicId}/approve` (RPC 스타일)
- B. `PUT /club-requests/{publicId}` (전체 교체 의미론)
- C. `PATCH /club-requests/{publicId}/approve` ← 선택

**결정**: C — PATCH + 동사 서브리소스 패턴

**결과/영향**:
- `PATCH` = 부분 수정, `/approve` 서브리소스 = 명확한 행위 표현
- 승인과 거절이 대칭적인 URL 구조(`/approve`, `/reject`)로 표현됨
- RESTful 원칙과 행위 명시성 사이의 실용적 균형

**후속 작업**: 없음