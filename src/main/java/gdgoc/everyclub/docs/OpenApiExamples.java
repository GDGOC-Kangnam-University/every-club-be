package gdgoc.everyclub.docs;

public final class OpenApiExamples {
    public static final String SUCCESS_VOID_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": null
            }
            """;

    public static final String USER_CREATE_REQUEST = """
            {
              "signupToken": "signup-token-example",
              "nickname": "동아리운영자",
              "studentId": "202401234",
              "password": "P@ssw0rd123!",
              "passwordConfirm": "P@ssw0rd123!"
            }
            """;

    public static final String USER_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": {
                "id": 42,
                "email": "student@kangnam.ac.kr",
                "nickname": "동아리운영자",
                "profileImageUrl": "https://cdn.everyclub.app/users/42/profile.png",
                "department": "컴퓨터공학과",
                "studentId": "202401234",
                "phoneNumber": "010-1234-5678",
                "bio": "학생 커뮤니티를 만드는 일에 관심이 있습니다.",
                "role": "USER",
                "createdAt": "2026-03-16T09:00:00",
                "updatedAt": "2026-03-16T10:30:00"
              }
            }
            """;

    public static final String UPDATE_CLUB_REQUEST = """
            {
              "name": "강남대 GDGoC",
              "summary": "개발 스터디와 프로젝트를 함께하는 동아리입니다.",
              "description": "이번 학기 모집에 맞춰 소개 문구를 수정한 예시입니다.",
              "logoUrl": "https://cdn.everyclub.app/clubs/gdgoc/logo.png",
              "bannerUrl": "https://cdn.everyclub.app/clubs/gdgoc/banner-spring.png",
              "joinFormUrl": "https://forms.gle/gdgoc-spring",
              "recruitingStatus": "OPEN",
              "majorId": 101,
              "activityCycle": "매주 목요일 저녁",
              "hasFee": false,
              "isPublic": true,
              "tags": ["개발", "안드로이드", "커뮤니티"]
            }
            """;

    public static final String CREATE_CLUB_REQUEST_REVIEW = """
            {
              "name": "스타트업 빌더스",
              "categoryId": 4,
              "slug": "startup-builders",
              "summary": "창업 워크숍과 팀 프로젝트 중심의 동아리 신청 예시입니다.",
              "description": "창업가 초청 세션, 피칭 연습, 팀 매칭 프로그램을 운영하려고 합니다.",
              "logoUrl": "https://cdn.everyclub.app/requests/startup/logo.png",
              "bannerUrl": "https://cdn.everyclub.app/requests/startup/banner.png",
              "joinFormUrl": "https://forms.gle/startup-builders",
              "recruitingStatus": "OPEN",
              "majorId": 101,
              "activityCycle": "격주 금요일",
              "hasFee": true,
              "isPublic": false,
              "tags": ["개발", "스터디", "친목"]
            }
            """;

    public static final String REJECT_CLUB_REQUEST = """
            {
              "adminMemo": "활동 계획과 지도교수 확인 내용을 조금 더 구체적으로 보완해 주세요."
            }
            """;

    public static final String CLUB_REGISTRATION_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": {
                "publicId": "123e4567-e89b-12d3-a456-426614174000",
                "status": "PENDING",
                "submittedAt": "2026-03-16T12:30:00"
              }
            }
            """;

    public static final String CLUB_APPROVAL_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": {
                "publicId": "123e4567-e89b-12d3-a456-426614174000",
                "status": "APPROVED",
                "submittedAt": "2026-03-16T12:30:00"
              }
            }
            """;

    public static final String CLUB_REJECTION_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": {
                "publicId": "123e4567-e89b-12d3-a456-426614174000",
                "status": "REJECTED",
                "submittedAt": "2026-03-16T12:30:00"
              }
            }
            """;

    public static final String USER_UPDATE_REQUEST = """
            {
              "nickname": "동아리운영자",
              "department": "컴퓨터공학과",
              "studentId": "202401234",
              "phoneNumber": "010-1234-5678",
              "bio": "학생 커뮤니티를 만드는 일에 관심이 있습니다."
            }
            """;

    public static final String CHECK_EMAIL_REQUEST = """
            {
              "email": "student@kangnam.ac.kr"
            }
            """;

    public static final String CHECK_EMAIL_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": true
            }
            """;

    public static final String PRESIGNED_UPLOAD_REQUEST = """
            {
              "fileName": "club-banner.png",
              "contentType": "image/png"
            }
            """;

    public static final String PRESIGNED_UPLOAD_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": {
                "presignedUrl": "https://storage.googleapis.com/bucket/object?signature=abc123"
              }
            }
            """;

    public static final String PRESIGNED_DOWNLOAD_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": "https://storage.googleapis.com/bucket/object?signature=abc123"
            }
            """;

    public static final String SIGNUP_SEND_OTP_REQUEST = """
            {
              "email": "student@kangnam.ac.kr"
            }
            """;

    public static final String SIGNUP_VERIFY_OTP_REQUEST = """
            {
              "email": "student@kangnam.ac.kr",
              "code": "123456"
            }
            """;

    public static final String SIGNUP_TOKEN_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": {
                "signupToken": "signup-token-example",
                "expiresIn": 600
              }
            }
            """;

    public static final String LOGIN_REQUEST = """
            {
              "email": "student@kangnam.ac.kr",
              "password": "P@ssw0rd123!"
            }
            """;

    public static final String LOGIN_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": {
                "grantType": "Bearer",
                "accessToken": "jwt-access-token-example",
                "expiresIn": 7200
              }
            }
            """;

    public static final String ADD_CLUB_ADMIN_REQUEST = """
            {
              "userId": 42
            }
            """;

    public static final String DELEGATE_CLUB_ADMIN_REQUEST = """
            {
              "targetUserId": 42,
              "formerLeaderAction": "DEMOTE"
            }
            """;

    public static final String CLUB_ADMIN_LIST_RESPONSE = """
            {
              "status": "SUCCESS",
              "message": null,
              "data": [
                {
                  "userId": 42,
                  "nickname": "동아리운영자",
                  "role": "LEAD",
                  "joinedAt": "2026-03-16T09:00:00"
                }
              ]
            }
            """;

    private OpenApiExamples() {
    }
}
