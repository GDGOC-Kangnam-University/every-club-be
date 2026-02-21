package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ClubRequestPayload(
        String name,
        Long categoryId,
        String slug,
        String summary,
        String description,
        String logoUrl,
        String bannerUrl,
        String joinFormUrl,
        RecruitingStatus recruitingStatus,
        Long majorId,
        String activityCycle,
        boolean hasFee,
        boolean isPublic,
        List<Long> tagIds
) {
}
