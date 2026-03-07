package gdgoc.everyclub.user.dto;

import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.domain.UserRole;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponse {
    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final String department;
    private final String studentId;
    private final String phoneNumber;
    private final String bio;
    private final UserRole role;
    private final LocalDateTime createdAt;
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
