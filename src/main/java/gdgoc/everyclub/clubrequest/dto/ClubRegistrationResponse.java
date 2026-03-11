package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClubRegistrationResponse(
        UUID publicId,
        RequestStatus status,
        LocalDateTime createdAt
) {
    public static ClubRegistrationResponse from(ClubRequest entity) {
        return new ClubRegistrationResponse(
                entity.getPublicId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
