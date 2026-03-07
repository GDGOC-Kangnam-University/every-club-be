package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ClubSummaryResponse {
    private final Long id;
    private final String slug;
    private final String name;
    private final String summary;
    private final String logoUrl;
    private final RecruitingStatus recruitingStatus;
    private final String activityCycle;
    private final boolean hasFee;
    private final int likeCount;
    private final Long categoryId;
    private final String categoryName;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ClubSummaryResponse(Club club) {
        this(club, 0);
    }

    public ClubSummaryResponse(Club club, int likeCount) {
        this.id = club.getId();
        this.slug = club.getSlug();
        this.name = club.getName();
        this.summary = club.getSummary();
        this.logoUrl = club.getLogoUrl();
        this.recruitingStatus = club.getRecruitingStatus();
        this.activityCycle = club.getActivityCycle();
        this.hasFee = club.isHasFee();
        this.likeCount = likeCount;
        this.categoryId = club.getCategory() != null ? club.getCategory().getId() : null;
        this.categoryName = club.getCategory() != null ? club.getCategory().getName() : null;
        this.authorName = club.getAuthor() != null ? club.getAuthor().getName() : "Unknown";
        this.createdAt = club.getCreatedAt();
        this.updatedAt = club.getUpdatedAt();
    }
}
