package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "내 동아리 등록 신청 응답")
public record ClubRequestMyResponse(
        @Schema(description = "신청 공개 식별자") UUID publicId,
        @Schema(description = "신청한 동아리 이름") String clubName,
        @Schema(description = "처리 상태") RequestStatus status,
        @Schema(description = "반려 사유 (REJECTED일 때만 존재)", nullable = true) String adminMemo,
        @Schema(description = "신청 시각") LocalDateTime createdAt,
        @Schema(description = "처리 시각", nullable = true) LocalDateTime reviewedAt
) {
    public static ClubRequestMyResponse from(ClubRequest request, String clubName) {
        return new ClubRequestMyResponse(
                request.getPublicId(),
                clubName,
                request.getStatus(),
                request.getStatus() == RequestStatus.REJECTED ? request.getAdminMemo() : null,
                request.getCreatedAt(),
                request.getReviewedAt()
        );
    }
}