package gdgoc.everyclub.club.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "동아리 관리자 추가 요청")
public record AddClubAdminRequest(
        @Schema(description = "관리자로 추가할 사용자 id", example = "42")
        @NotNull Long userId
) {
}
