package gdgoc.everyclub.docs;

public final class OpenApiExamples {
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

    public static final String USER_UPDATE_REQUEST = """
            {
              "nickname": "동아리운영자",
              "department": "컴퓨터공학과",
              "studentId": "20241234",
              "phoneNumber": "010-1234-5678",
              "bio": "학생 커뮤니티를 만드는 일에 관심이 있습니다."
            }
            """;

    public static final String CHECK_EMAIL_REQUEST = """
            {
              "email": "student@kangnam.ac.kr"
            }
            """;

    public static final String PRESIGNED_UPLOAD_REQUEST = """
            {
              "fileName": "club-banner.png",
              "contentType": "image/png"
            }
            """;

    private OpenApiExamples() {
    }
}
