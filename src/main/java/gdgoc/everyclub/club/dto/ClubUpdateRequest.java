package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record ClubUpdateRequest(
        @NotBlank(message = "Name must not be empty")
        @Size(max = 20, message = "Name must be less than 20 characters")
        String name,

        @NotBlank(message = "Summary must not be empty")
        @Size(max = 200, message = "Summary must be less than 200 characters")
        String summary,

        String description,

        @Size(max = 2048)
        String logoUrl,

        @Size(max = 2048)
        String bannerUrl,

        @Size(max = 2048)
        String joinFormUrl,

        @NotNull(message = "Recruiting status must not be null")
        RecruitingStatus recruitingStatus,

        @Size(max = 50)
        String department,

        @Size(max = 50)
        String activityCycle,

        boolean hasFee,

        boolean isPublic,

        List<String> tags
) {
}
