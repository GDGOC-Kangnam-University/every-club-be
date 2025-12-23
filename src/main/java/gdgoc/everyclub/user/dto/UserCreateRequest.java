package gdgoc.everyclub.user.dto;


import jakarta.validation.constraints.NotEmpty;

public record UserCreateRequest(
        @NotEmpty(message = "Name must not be empty")
        String name,
        @NotEmpty(message = "Email must not be empty")
        String email) {
}