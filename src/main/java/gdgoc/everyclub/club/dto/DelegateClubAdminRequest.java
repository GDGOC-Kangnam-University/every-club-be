package gdgoc.everyclub.club.dto;

import jakarta.validation.constraints.NotNull;

public record DelegateClubAdminRequest(
        @NotNull Long targetUserId,
        @NotNull FormerLeaderAction formerLeaderAction
) {
    public enum FormerLeaderAction {
        DEMOTE,  // 기존 LEAD를 MEMBER로 강등
        REMOVE   // 기존 LEAD를 관리자 목록에서 제거
    }
}