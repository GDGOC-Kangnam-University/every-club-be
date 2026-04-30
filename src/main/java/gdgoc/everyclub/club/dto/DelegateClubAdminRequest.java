package gdgoc.everyclub.club.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "동아리 LEAD 위임 요청")
public record DelegateClubAdminRequest(
        @Schema(description = "LEAD 권한을 받을 사용자 id", example = "42")
        @NotNull Long targetUserId,

        @Schema(description = "기존 LEAD 처리 방식", example = "DEMOTE")
        @NotNull FormerLeaderAction formerLeaderAction
) {
    @Schema(description = "기존 LEAD 처리 방식")
    public enum FormerLeaderAction {
        DEMOTE,  // 기존 LEAD를 MEMBER로 강등
        REMOVE   // 기존 LEAD를 관리자 목록에서 제거
    }
}
