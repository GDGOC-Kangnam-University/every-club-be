# 변경 이력 (CHANGES)

---

## 2026-02-21 — 동아리 등록 신청 및 관리자 승인 워크플로우

**브랜치**: `feat/club-registration`

**요약**:
로그인 사용자가 동아리 등록을 신청하고, 시스템 관리자가 승인/거절하는 전체 워크플로우를 구현했다.
승인 시 ClubRequest payload에서 Club 엔티티를 자동 생성한다.
또한 학과동아리 지원을 위해 College/Major 마스터 데이터 모듈을 추가하고,
Club 도메인에 Major 연관관계를 반영했다.

---

### API 변경

#### 신규 엔드포인트

| Method | URL | 설명 | 비고 |
|--------|-----|------|------|
| `POST` | `/club-requests` | 동아리 등록 신청 | JWT 인증 필요 |
| `GET` | `/club-requests` | 신청 목록 조회 | 관리자 전용 |
| `GET` | `/club-requests/{publicId}` | 신청 단건 조회 | 관리자 전용 |
| `PATCH` | `/club-requests/{publicId}/approve` | 신청 승인 + Club 생성 | 관리자 전용 |
| `PATCH` | `/club-requests/{publicId}/reject` | 신청 거절 | 관리자 전용 |
| `GET` | `/colleges` | 단과대학 + 학과 목록 | 공개 |

#### Request Body

**`POST /club-requests`**
```json
{
  "name": "string (1~20)",
  "categoryId": "long",
  "slug": "string (1~100)",
  "summary": "string (1~200)",
  "description": "string (optional)",
  "logoUrl": "string (optional)",
  "bannerUrl": "string (optional)",
  "joinFormUrl": "string (optional)",
  "recruitingStatus": "OPEN | CLOSED",
  "majorId": "long (학과동아리 필수, 그 외 optional)",
  "activityCycle": "string (optional)",
  "hasFee": "boolean",
  "isPublic": "boolean",
  "tagIds": "[long] (optional)"
}
```

**`PATCH /club-requests/{publicId}/reject`**
```json
{
  "adminMemo": "string (1~500, 필수)"
}
```

#### Response

**`POST /club-requests`** / **`PATCH .../approve`** / **`PATCH .../reject`**
```json
{
  "success": true,
  "data": {
    "publicId": "uuid",
    "status": "PENDING | APPROVED | REJECTED",
    "createdAt": "datetime"
  }
}
```

**`GET /club-requests/{publicId}`** (관리자)
```json
{
  "success": true,
  "data": {
    "publicId": "uuid",
    "requesterName": "string",
    "requesterEmail": "string",
    "payload": { /* ClubRequestPayload 필드 전체 */ },
    "status": "PENDING | APPROVED | REJECTED",
    "adminMemo": "string | null",
    "reviewerName": "string | null",
    "reviewedAt": "datetime | null",
    "createdAt": "datetime"
  }
}
```

---

### DB 변경

#### 신규 테이블

| 테이블 | 설명 |
|--------|------|
| `college` | 단과대학 마스터 (id, name) |
| `major` | 학과 마스터 (id, name, college_id FK) |
| `club_request` | 동아리 등록 신청 (id, public_id, requested_by, payload JSON, status, admin_memo, reviewed_by, reviewed_at, created_at) |

#### 변경된 테이블

| 테이블 | 변경 내용 |
|--------|-----------|
| `club` | `major_id` 컬럼 추가 (`major` 테이블 FK, nullable) |

---

### 마이그레이션/주의사항

- **시드 데이터**: 애플리케이션 시작 시 `DataInitializer`가 `college`, `major` 테이블을 자동 초기화함. 중복 실행 방지는 `existsByName` 체크로 처리.
- **club.major_id**: 기존 Club 레코드는 `major_id = NULL` (중앙동아리 또는 마이그레이션 전 데이터). 스키마 변경 시 기존 데이터 영향 없음.
- **SecurityContext 의존**: 승인/거절 엔드포인트는 `@AuthenticationPrincipal CustomUserDetails`로 관리자 userId를 추출. JWT 필터가 정상 동작해야 함.