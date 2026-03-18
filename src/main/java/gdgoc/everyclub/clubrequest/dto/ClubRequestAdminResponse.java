package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Administrative view of a club request")
public record ClubRequestAdminResponse(
        @Schema(description = "Public request id", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID publicId,
        @Schema(description = "Requester name", example = "Kim Student")
        String requesterName,
        @Schema(description = "Requester email", example = "student@university.ac.kr")
        String requesterEmail,
        @Schema(description = "Stored request payload")
        ClubRequestPayload payload,
        @Schema(description = "Current request status", example = "PENDING")
        RequestStatus status,
        @Schema(description = "Admin memo for review or rejection", nullable = true, example = "Need a clearer club schedule.")
        String adminMemo,
        @Schema(description = "Reviewer name", nullable = true, example = "Admin User")
        String reviewerName,
        @Schema(description = "Review timestamp", nullable = true, example = "2026-03-16T13:00:00")
        LocalDateTime reviewedAt,
        @Schema(description = "Submission time", example = "2026-03-16T12:30:00")
        LocalDateTime createdAt
) {
    public static ClubRequestAdminResponse from(ClubRequest entity, ClubRequestPayload payload) {
        return new ClubRequestAdminResponse(
                entity.getPublicId(),
                entity.getRequestedBy().getName(),
                entity.getRequestedBy().getEmail(),
                payload,
                entity.getStatus(),
                entity.getAdminMemo(),
                entity.getReviewedBy() != null ? entity.getReviewedBy().getName() : null,
                entity.getReviewedAt(),
                entity.getCreatedAt()
        );
    }
}
