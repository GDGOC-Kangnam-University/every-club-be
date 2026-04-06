package gdgoc.everyclub.user.dto;

import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "내 프로필 응답")
public class UserResponse {
    @Schema(description = "사용자 id", example = "42")
    private final Long id;
    @Schema(description = "이메일 주소", example = "student@kangnam.ac.kr")
    private final String email;
    @Schema(description = "닉네임", example = "동아리운영자")
    private final String nickname;
    @Schema(description = "프로필 이미지 URL", nullable = true, example = "https://cdn.everyclub.app/users/42/profile.png")
    private final String profileImageUrl;
    @Schema(description = "학과명", nullable = true, example = "컴퓨터공학과")
    private final String department;
    @Schema(description = "학번", nullable = true, example = "20241234")
    private final String studentId;
    @Schema(description = "전화번호", nullable = true, example = "010-1234-5678")
    private final String phoneNumber;
    @Schema(description = "자기소개", nullable = true, example = "학생 커뮤니티를 만드는 일에 관심이 있습니다.")
    private final String bio;
    @Schema(description = "사용자 권한", example = "USER")
    private final UserRole role;
    @Schema(description = "계정 생성 시각", example = "2026-03-16T09:00:00")
    private final LocalDateTime createdAt;
    @Schema(description = "최근 프로필 수정 시각", example = "2026-03-16T10:30:00")
    private final LocalDateTime updatedAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.department = user.getDepartment();
        this.studentId = user.getStudentId();
        this.phoneNumber = user.getPhoneNumber();
        this.bio = user.getBio();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}
