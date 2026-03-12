package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClubRequestAdminResponse(
        UUID publicId,
        String requesterName,
        String requesterEmail,
        ClubRequestPayload payload,
        RequestStatus status,
        String adminMemo,
        String reviewerName,
        LocalDateTime reviewedAt,
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