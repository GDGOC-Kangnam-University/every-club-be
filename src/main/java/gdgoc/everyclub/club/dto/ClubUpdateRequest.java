package gdgoc.everyclub.club.dto;

import jakarta.validation.constraints.NotEmpty;

public record ClubUpdateRequest(
        @NotEmpty(message = "Title must not be empty")
        String title,
        @NotEmpty(message = "Content must not be empty")
        String content
) {
}
