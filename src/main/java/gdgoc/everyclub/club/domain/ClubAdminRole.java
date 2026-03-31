package gdgoc.everyclub.club.domain;

public enum ClubAdminRole {
    LEAD,   // 대표 관리자: 수정, 삭제, 관리자 추가/제거, 위임 가능
    MEMBER  // 일반 관리자: 수정만 가능
}