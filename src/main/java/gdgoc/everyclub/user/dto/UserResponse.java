package gdgoc.everyclub.user.dto;

import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.domain.UserRole;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String name;
    private final String email;
    private final UserRole role;

    public UserResponse(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
