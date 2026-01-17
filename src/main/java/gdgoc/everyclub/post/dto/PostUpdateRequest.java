package gdgoc.everyclub.post.dto;

import jakarta.validation.constraints.NotEmpty;

public record PostUpdateRequest(
        @NotEmpty(message = "Title must not be empty")
        String title,
        @NotEmpty(message = "Content must not be empty")
        String content
) {
}
