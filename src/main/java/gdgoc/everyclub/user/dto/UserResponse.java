package gdgoc.everyclub.user.dto;

import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.domain.UserRole;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {

    private final UUID publicId;
    private final String name;
    private final String email;
    private final UserRole role;
    private final String studentNumber;

    public UserResponse(User user) {
        this.publicId = user.getPublicId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.studentNumber = user.getStudentNumber();
    }
}
