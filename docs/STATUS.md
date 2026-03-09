# 진행 상황 (STATUS)

## 현재 집중 중인 작업

**브랜치**: `feat/club-registration`
**태스크**: 동아리 등록 신청 및 관리자 승인 워크플로우 구현

---

## 완료된 작업

### college/ 모듈
- `College`, `Major` 엔티티 및 JPA Repository
- `DataInitializer`: 강남대학교 6개 단과대학, 19개 학과 시드 데이터

### clubrequest/ 모듈 — 사용자 측
- `ClubRequest` 엔티티 (JSON payload 방식)
- `RequestStatus` enum: `PENDING` → `APPROVED` / `REJECTED`
- `ClubRegistrationRequest` 입력 DTO (Bean Validation)
- `ClubRegistrationResponse` 응답 DTO
- `POST /club-requests` 엔드포인트

### clubrequest/ 모듈 — 관리자 측
- `ClubRequest.approve(admin)` / `reject(admin, memo)` 도메인 메서드
- `ClubRequestAdminResponse`, `ClubRequestRejectRequest` DTO
- `GET /club-requests` — 전체 목록 (N+1 방지: `@EntityGraph`)
- `GET /club-requests/{publicId}` — 단건 조회
- `PATCH /club-requests/{publicId}/approve` — 승인 (Club 자동 생성)
- `PATCH /club-requests/{publicId}/reject` — 거절

### Club 도메인 수정
- `Major` 필드 추가 (학과동아리용)
- `ClubCreateRequest`, `ClubUpdateRequest`, `ClubDetailResponse` DTO에 `majorId` 반영
- `ValidationErrorCode.MAJOR_REQUIRED_FOR_DEPARTMENT_CLUB` 추가

---

## 실행 방법

### 로컬 실행
```bash
./gradlew bootRun
```

### 테스트 실행
```bash
./gradlew test
```

### 테스트 + 커버리지 리포트
```bash
./gradlew jacocoTestReport
# 리포트: build/reports/jacoco/test/html/index.html
```

---

## 현재 막힌 점

없음.

---

## 다음 단계

1. **JWT 연동** — `ClubRequestController`의 `createClubRequest`는 `@AuthenticationPrincipal`로 구현 완료. Security 필터 설정과 연동 확인 필요.

2. **관리자 권한 보호** — `GET/PATCH /club-requests` 엔드포인트에 `SYSTEM_ADMIN` 역할 검증 추가 (`@PreAuthorize` 또는 Security Config에서 설정).

3. **ClubAdmin 자동 등록** — ERD 명세에 따르면 승인 시 신청자를 해당 동아리의 `ClubAdmin`으로 자동 등록해야 함. `ClubAdmin` 도메인이 아직 미구현 상태.

4. **이메일 인증 검증** — ERD에서 `email_verified = true`인 사용자만 동아리 신청 가능. 현재 `ClubRequestService.createClubRequest`에서 미검증.

5. **페이지네이션** — `GET /club-requests` 목록 조회에 페이지네이션 적용 검토 (요청 수가 많아질 경우).