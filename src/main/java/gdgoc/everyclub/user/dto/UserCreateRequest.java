package gdgoc.everyclub.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreateRequest {
    private String name;
    private String email;
    private String studentNumber;
}
