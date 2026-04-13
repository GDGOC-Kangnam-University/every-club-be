package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "제출된 동아리 승인 요청 요약")
public record ClubRegistrationResponse(
        @Schema(description = "공개용 요청 id", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID publicId,
        @Schema(description = "현재 요청 상태", example = "PENDING")
        RequestStatus status,
        @Schema(description = "제출 시각", example = "2026-03-16T12:30:00")
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
