package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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
    private final Long majorId;
    private final String majorName;
    private final String collegeName;
    private final String activityCycle;
    private final boolean hasFee;
    private final boolean isPublic;
    private final boolean isLiked;
    private final int likeCount;
    private final List<String> tags;
    private final Long categoryId;
    private final String categoryName;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ClubDetailResponse(Club club) {
        this(club, false, 0);
    }

    public ClubDetailResponse(Club club, boolean isLiked, int likeCount) {
        this.id = club.getId();
        this.slug = club.getSlug();
        this.name = club.getName();
        this.summary = club.getSummary();
        this.description = club.getDescription();
        this.logoUrl = club.getLogoUrl();
        this.bannerUrl = club.getBannerUrl();
        this.joinFormUrl = club.getJoinFormUrl();
        this.recruitingStatus = club.getRecruitingStatus();
        this.majorId = club.getMajor() != null ? club.getMajor().getId() : null;
        this.majorName = club.getMajor() != null ? club.getMajor().getName() : null;
        this.collegeName = club.getMajor() != null ? club.getMajor().getCollege().getName() : null;
        this.activityCycle = club.getActivityCycle();
        this.hasFee = club.isHasFee();
        this.isPublic = club.isPublic();
        this.isLiked = isLiked;
        this.likeCount = likeCount;
        this.tags = club.getTagNames();
        this.categoryId = club.getCategory() != null ? club.getCategory().getId() : null;
        this.categoryName = club.getCategory() != null ? club.getCategory().getName() : null;
        this.authorName = club.getAuthor() != null ? club.getAuthor().getName() : "Unknown";
        this.createdAt = club.getCreatedAt();
        this.updatedAt = club.getUpdatedAt();
    }
}
