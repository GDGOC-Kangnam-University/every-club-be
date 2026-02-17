package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ClubDetailResponse {
    private final Long id;
    private final String slug;
    private final String name;
    private final String summary;
    private final String description;
    private final String logoUrl;
    private final String bannerUrl;
    private final String joinFormUrl;
    private final RecruitingStatus recruitingStatus;
    private final String department;
    private final String activityCycle;
    private final boolean hasFee;
    private final boolean isPublic;
    private final int likeCount;
    private final Long categoryId;
    private final String categoryName;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ClubDetailResponse(Club club) {
        this.id = club.getId();
        this.slug = club.getSlug();
        this.name = club.getName();
        this.summary = club.getSummary();
        this.description = club.getDescription();
        this.logoUrl = club.getLogoUrl();
        this.bannerUrl = club.getBannerUrl();
        this.joinFormUrl = club.getJoinFormUrl();
        this.recruitingStatus = club.getRecruitingStatus();
        this.department = club.getDepartment();
        this.activityCycle = club.getActivityCycle();
        this.hasFee = club.isHasFee();
        this.isPublic = club.isPublic();
        this.likeCount = club.getLikeCount();
        this.categoryId = club.getCategory() != null ? club.getCategory().getId() : null;
        this.categoryName = club.getCategory() != null ? club.getCategory().getName() : null;
        this.authorName = club.getAuthor() != null ? club.getAuthor().getName() : "Unknown";
        this.createdAt = club.getCreatedAt();
        this.updatedAt = club.getUpdatedAt();
    }
}
