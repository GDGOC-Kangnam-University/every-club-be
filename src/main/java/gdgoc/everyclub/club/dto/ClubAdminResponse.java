package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.ClubAdmin;
import gdgoc.everyclub.club.domain.ClubAdminRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "동아리 관리자 응답")
public record ClubAdminResponse(
        @Schema(description = "사용자 id", example = "42")
        Long userId,

        @Schema(description = "사용자 닉네임", example = "동아리운영자")
        String nickname,

        @Schema(description = "동아리 관리자 역할", example = "LEAD")
        ClubAdminRole role,

        @Schema(description = "관리자 등록 시각", example = "2026-03-16T09:00:00")
        LocalDateTime joinedAt
) {
    public static ClubAdminResponse from(ClubAdmin clubAdmin) {
        return new ClubAdminResponse(
                clubAdmin.getUser().getId(),
                clubAdmin.getUser().getNickname(),
                clubAdmin.getRole(),
                clubAdmin.getCreatedAt()
        );
    }
}
