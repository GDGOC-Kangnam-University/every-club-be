package gdgoc.everyclub.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank(message = "Name must not be blank")
        String name
) {
}
