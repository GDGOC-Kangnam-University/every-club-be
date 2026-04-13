package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.ClubAdmin;
import gdgoc.everyclub.club.domain.ClubAdminRole;

import java.time.LocalDateTime;

public record ClubAdminResponse(
        Long userId,
        String nickname,
        ClubAdminRole role,
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