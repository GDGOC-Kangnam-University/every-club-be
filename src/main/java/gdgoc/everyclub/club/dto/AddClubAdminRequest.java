package gdgoc.everyclub.club.dto;

import jakarta.validation.constraints.NotNull;

public record AddClubAdminRequest(
        @NotNull Long userId
) {
}