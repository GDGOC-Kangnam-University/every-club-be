package gdgoc.everyclub.club.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ClubCreateRequest(
        @NotEmpty(message = "Title must not be empty")
        String title,
        @NotEmpty(message = "Content must not be empty")
        String content,
        @NotNull(message = "Author ID must not be null")
        Long authorId
) {
}
