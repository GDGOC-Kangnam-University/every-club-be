// =============================================
// 1. 사용자 (User Entity)
// =============================================
// 학교 이메일(@xxx.ac.kr)로만 가입이 허용됩니다.
// 역할(role)은 글로벌 권한(GUEST, SYSTEM_ADMIN)만 관리합니다.
// 동아리별 관리 권한은 ClubAdmin 테이블에서 별도 관리합니다.
// 이메일 인증이 완료되지 않은 사용자는 로그인은 가능하지만 동아리 생성/찜 등 핵심 기능은 제한됩니다.
// 사용자 탈퇴 시 소프트 삭제 + 익명화 전략을 사용합니다.

Table User {
id long [pk, increment, not null, unique] // 대체키(PK): 내부 식별용, 외부 노출 금지
public_id uuid [not null, unique] // 공개 식별자: API 응답, URL 등 외부 노출용. Sequential ID 노출로 인한 보안 리스크 방지

nickname varchar(10) [not null] // 실명검증 없으므로 닉네임
email varchar(320) [not null, unique] // 학교 이메일: RFC 5321 표준 최대 길이. 로그인 식별자 + 가입 자격 검증(@xxx.ac.kr). 탈퇴 시 익명화(deleted_{id}@invalid)
password varchar(60) [not null] // 비밀번호: bcrypt 해시 저장(60자 고정). 평문 저장 절대 금지
email_verified boolean [not null, default: false] // 이메일 인증 여부: 가입 후 학교 이메일로 인증 링크 발송, 인증 완료 시 true
email_verified_at datetime [null] // 이메일 인증 완료 시점: 인증 전 null. 감사/통계용

role enum [not null, default: 'GUEST', note: 'GUEST(일반), SYSTEM_ADMIN(시스템 관리자)'] // 글로벌 권한: 동아리별 권한은 ClubAdmin에서 관리
student_number varchar(15) [null, unique] // 학번: 학교별 형식 상이(숫자/문자 혼합 가능). 선택 입력 시 nullable

created_at datetime [not null, default: 'now()'] // 생성일: 최초 가입 시점. 감사/통계/약관 대응
updated_at datetime [not null, default: 'now()'] // 수정일: 프로필 변경 추적, 캐시 무효화/동기화. 모든 필드 변경 시 자동 갱신
deleted_at datetime [null] // 탈퇴일(소프트 삭제): 계정 비활성화 + 개인정보보호법 유예 기간 대응. 탈퇴 시 email/name 익명화 처리
}

// =============================================
// 2. 인증 토큰 (VerificationToken Entity)
// =============================================
// 이메일 인증 및 비밀번호 재설정용 토큰을 저장하는 테이블입니다.
// 하나의 테이블에서 토큰 유형(type)으로 구분하여 여러 인증 시나리오를 통합 관리합니다.
// 토큰은 일회용이며, 사용 완료 시 used_at에 시점을 기록하여 재사용을 방지합니다.

Table VerificationToken {
id long [pk, increment, not null, unique] // 대체키(PK)
user_id long [ref: > User.id, not null] // 사용자 FK: 토큰 소유자

token varchar(255) [not null, unique] // 토큰 값: UUID 또는 SecureRandom 기반 문자열. URL에 포함되어 전달됨
type enum [not null, note: 'EMAIL_VERIFICATION(이메일 인증), PASSWORD_RESET(비밀번호 재설정)'] // 토큰 유형: 향후 다른 유형 추가 가능

expires_at datetime [not null] // 만료 시점: 일반적으로 이메일 인증 24시간, 비밀번호 재설정 1시간
created_at datetime [not null, default: 'now()'] // 생성 시점: 토큰 발급 시점
used_at datetime [null] // 사용 시점: 토큰 사용 완료 시 기록. null이면 미사용. 재사용 방지용
}

// =============================================
// 3. 동아리 (Club Entity)
// =============================================
// 동아리의 핵심 정보입니다.
// 공개 여부(is_public)는 System Admin 승인 후 true가 되며, 동아리 관리자가 비공개 전환 가능합니다.
// slug는 URL 경로에 사용되는 고유 식별자로, 한 번 생성되면 변경할 수 없습니다.

Table Club {
id long [pk, increment, not null, unique] // 대체키(PK): 내부 식별용
public_id uuid [not null, unique] // 공개 식별자: API 응답용. Sequential ID 노출 방지

name varchar(20) [not null] // 동아리명: 목록/카드에 표시
slug varchar(100) [not null, unique] // URL 슬러그: 소문자 + 하이픈 조합 (e.g., /club/awesome-band). URL-safe 검증 필요. 생성 후 변경 불가
summary varchar(200) [not null] // 한 줄 소개: 목록 페이지 카드 UI용 (2~3줄)
description text [null] // 상세 소개: 마크다운/HTML 지원. 상세 페이지용. 길이 제한은 앱 레벨에서 처리

logo_url varchar(2048) [null] // 로고 이미지: S3 URL 또는 CDN 경로. URL 표준 최대 길이 대응
banner_url varchar(2048) [null] // 배너 이미지: 상세 페이지 상단용
join_form_url varchar(2048) [null] // 가입 신청 링크: 구글폼 등 외부 폼 URL

recruiting_status enum [not null, default: 'OPEN', note: 'OPEN(모집중), CLOSED(모집마감)'] // 모집 상태: 필터링/배지 표시용

department varchar(50) [null] // 소속 학과: null이면 중앙동아리(전체 대상). 학과 필터용
activity_cycle varchar(50) [null] // 활동 주기: 자유 텍스트 (e.g., "매주 목요일 19시", "월 2회")
has_fee boolean [not null, default: false] // 회비 유무: 검색 필터용. 금액은 description에 기재

is_public boolean [not null, default: false] // 공개 여부: System Admin 승인 후 true. 동아리 관리자가 비공개 전환 가능
like_count int [not null, default: 0] // 찜 수: 비정규화 컬럼. 인기순 정렬, 목록 표시용. ClubLike 변경 시 동기화 필요

category_id long [ref: > Category.id, not null] // 분류 FK: 중앙/학과/자율 등 (1:N)
created_by long [ref: > User.id, not null] // 최초 등록 신청자: 동아리 생성 이력 추적용

created_at datetime [not null, default: 'now()'] // 생성일
updated_at datetime [not null, default: 'now()'] // 수정일: 모든 필드 변경 시 자동 갱신 (JPA @UpdateTimestamp 또는 DB trigger)
deleted_at datetime [null] // 삭제일(소프트 삭제): 동아리 폐쇄/삭제 시 사용. 삭제된 동아리는 목록에서 제외
}

// =============================================
// 4. 동아리 활동/프로젝트 (ClubActivity)
// =============================================
// 상세 페이지의 "프로젝트/스터디" 탭 콘텐츠입니다.
// type enum으로 탭을 구분합니다.
// display_order로 관리자가 순서를 직접 지정할 수 있습니다.

Table ClubActivity {
id long [pk, increment] // 대체키(PK)
club_id long [ref: > Club.id, not null] // 소속 동아리 FK

title varchar(100) [not null] // 활동 제목
description text [null] // 활동 내용: 링크 포함 가능. 마크다운 지원 고려
image_url varchar(2048) [null] // 대표 이미지: S3/CDN 경로

type enum [not null, note: 'PROJECT(프로젝트), STUDY(스터디)'] // 탭 구분용. 향후 EVENT, COMPETITION 등 확장 가능
display_order int [not null, default: 0] // 정렬 순서: 낮을수록 먼저 표시. 관리자가 드래그앤드롭으로 순서 변경 가능
created_at datetime [not null, default: 'now()'] // 생성일: 활동 등록 시점. 감사/정렬 기준
}

// =============================================
// 5. 동아리 갤러리 (ClubImage)
// =============================================
// 상세 페이지 갤러리 섹션용 이미지입니다.
// display_order로 정렬 순서를 관리합니다.
// 이미지 개수 제한(예: 최대 10장)은 앱 레벨에서 처리합니다.

Table ClubImage {
id long [pk, increment] // 대체키(PK)
club_id long [ref: > Club.id, not null] // 소속 동아리 FK
image_url varchar(2048) [not null] // 이미지 URL: S3/CDN 경로. URL 표준 최대 길이 대응
display_order int [not null, default: 0] // 정렬 순서: 낮을수록 먼저 표시. 드래그앤드롭 정렬 지원용
created_at datetime [not null, default: 'now()'] // 업로드 시점: 이미지 등록 시점. 감사/기본 정렬 기준
}

// =============================================
// 6. 동아리 찜/관심 (ClubLike)
// =============================================
// 마이페이지 "찜한 동아리" 기능용입니다.
// user_id + club_id 복합 UNIQUE로 중복 찜을 방지합니다.
// Club.like_count와 동기화하여 비정규화 데이터를 관리합니다.

Table ClubLike {
id long [pk, increment] // 대체키(PK): JPA 대리키 규칙 준수
user_id long [ref: > User.id, not null] // 찜한 사용자 FK
club_id long [ref: > Club.id, not null] // 찜한 동아리 FK
created_at datetime [not null, default: 'now()'] // 찜한 시점: 최신순 정렬용

Indexes {
(user_id, club_id) [unique, name: 'uk_club_like_user_club'] // 중복 찜 방지
}
}

// =============================================
// 7. 동아리 분류 (Category)
// =============================================
// 동아리 유형 분류 마스터 데이터입니다. (1:N 관계)
// 예: 중앙동아리, 학과동아리, 자율동아리

Table Category {
id long [pk, increment, not null, unique] // 대체키(PK)
name varchar(30) [not null, unique] // 분류명: "중앙동아리", "학과동아리", "자율동아리" 등. 중복 방지

// 마스터 데이터이므로 별도 인덱스 불필요 (name UNIQUE 제약이 인덱스 역할)
}

// =============================================
// 8. 동아리 태그 (Tag)
// =============================================
// 검색/필터링용 태그 마스터 데이터입니다. (N:M 관계)
// DB에는 # 미포함 저장, UI 표시 시 # 추가합니다.
// 예: DB "학술" → UI "#학술"

Table Tag {
id long [pk, increment, not null, unique] // 대체키(PK)
name varchar(30) [not null, unique] // 태그명: # 미포함 저장(정규화). "친목", "학술", "봉사" 등
}

// =============================================
// 9. 동아리-태그 매핑 (ClubTag)
// =============================================
// Club과 Tag의 N:M 관계를 정의하는 Junction Table입니다.
// club_id + tag_id 복합 UNIQUE로 중복 매핑을 방지합니다.

Table ClubTag {
id long [pk, increment] // 대체키(PK): JPA 대리키 규칙 준수
club_id long [ref: > Club.id, not null] // 동아리 FK
tag_id long [ref: > Tag.id, not null] // 태그 FK

Indexes {
(club_id, tag_id) [unique, name: 'uk_club_tag_club_tag'] // 중복 매핑 방지
}
}

// =============================================
// 10. 동아리 관리자 (ClubAdmin)
// =============================================
// 동아리별 관리 권한을 정의하는 Junction Table입니다. (N:M)
// User.role이 GUEST여도 이 테이블에 매핑되면 해당 Club에 한해 Admin 권한을 가집니다.
// 글로벌 권한(SYSTEM_ADMIN)과 분리하여 최소 권한 원칙을 적용합니다.
// user_id + club_id 복합 UNIQUE로 중복 권한 부여를 방지합니다.

Table ClubAdmin {
id long [pk, increment] // 대체키(PK): JPA 대리키 규칙 준수
user_id long [ref: > User.id, not null] // 관리자 FK
club_id long [ref: > Club.id, not null] // 동아리 FK
created_at datetime [not null, default: 'now()'] // 권한 부여 시점: 감사 로그용

Indexes {
(user_id, club_id) [unique, name: 'uk_club_admin_user_club'] // 중복 권한 부여 방지
}
}

// =============================================
// 11. 동아리 등록 신청 (ClubRequest)
// =============================================
// 로그인 사용자가 새 동아리를 등록 신청하는 플로우입니다.
// 승인 시 Club 테이블에 데이터 생성, 신청자는 자동으로 ClubAdmin에 등록됩니다.

Table ClubRequest {
id long [pk, increment, not null, unique] // 대체키(PK)
public_id uuid [not null, unique] // 공개 식별자: 신청 내역 조회 API용. Sequential ID 노출 방지

requested_by long [ref: > User.id, not null] // 신청자 FK
payload json [not null] // 신청 정보: Club 테이블 필드들을 JSON으로 저장. 앱 레벨에서 크기 제한 권장(예: 10KB)

status enum [not null, default: 'PENDING', note: 'PENDING(대기), APPROVED(승인), REJECTED(거절)'] // 처리 상태: 신청 워크플로우 상태 관리

admin_memo varchar(500) [null] // 관리자 메모: 거절 사유 등. 신청자에게 표시 가능
reviewed_by long [ref: > User.id, null] // 처리한 System Admin FK: 승인/거절 전 null
reviewed_at datetime [null] // 처리 시점: 승인/거절 전 null

created_at datetime [not null, default: 'now()'] // 신청 시점: 신청 이력 추적
}