package gdgoc.everyclub.clubrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for rejecting a club request")
public record ClubRequestRejectRequest(
        @Schema(description = "Reason for rejection shown to the requester", example = "Please provide a clearer activity plan and advisor confirmation.")
        @NotBlank(message = "Rejection reason is required")
        @Size(max = 500, message = "Rejection reason must be less than 500 characters")
        String adminMemo
) {
}
