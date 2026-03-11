package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record ClubRegistrationRequest(
        @NotBlank(message = "동아리명은 필수입니다")
        @Size(max = 20, message = "동아리명은 20자 이하여야 합니다")
        String name,

        @NotNull(message = "카테고리 ID는 필수입니다")
        Long categoryId,

        @NotBlank(message = "슬러그는 필수입니다")
        @Size(max = 100, message = "슬러그는 100자 이하여야 합니다")
        String slug,

        @NotBlank(message = "한 줄 소개는 필수입니다")
        @Size(max = 200, message = "한 줄 소개는 200자 이하여야 합니다")
        String summary,

        String description,

        @Size(max = 2048)
        String logoUrl,

        @Size(max = 2048)
        String bannerUrl,

        @Size(max = 2048)
        String joinFormUrl,

        @NotNull(message = "모집 상태는 필수입니다")
        RecruitingStatus recruitingStatus,

        Long majorId,

        @Size(max = 50)
        String activityCycle,

        boolean hasFee,

        boolean isPublic,

        List<Long> tagIds
) {
}
