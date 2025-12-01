package gdgoc.everyclub.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "Name must not be blank")
    private String name;

    @Pattern(regexp = "^[0-9]{8}$", message = "Student number must be 8 digits")
    private String studentNumber;
}
