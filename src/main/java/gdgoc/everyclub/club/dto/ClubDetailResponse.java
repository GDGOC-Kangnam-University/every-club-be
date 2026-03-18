package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "동아리 상세 응답")
public class ClubDetailResponse {
    @Schema(description = "동아리 id", example = "1")
    private final Long id;
    @Schema(description = "동아리 slug", example = "gdgoc-knu")
    private final String slug;
    @Schema(description = "동아리 이름", example = "강남대 GDGoC")
    private final String name;
    @Schema(description = "목록에 노출되는 한 줄 소개", example = "개발 스터디와 프로젝트를 함께하는 동아리입니다.")
    private final String summary;
    @Schema(description = "상세 소개", nullable = true, example = "정기 스터디, 연사 세션, 사이드 프로젝트를 함께 진행합니다.")
    private final String description;
    @Schema(description = "로고 이미지 URL", nullable = true, example = "https://cdn.everyclub.app/clubs/gdgoc/logo.png")
    private final String logoUrl;
    @Schema(description = "배너 이미지 URL", nullable = true, example = "https://cdn.everyclub.app/clubs/gdgoc/banner.png")
    private final String bannerUrl;
    @Schema(description = "가입 신청 폼 URL", nullable = true, example = "https://forms.gle/join-gdgoc-knu")
    private final String joinFormUrl;
    @Schema(description = "모집 상태", example = "OPEN")
    private final RecruitingStatus recruitingStatus;
    @Schema(description = "학과 id", nullable = true, example = "101")
    private final Long majorId;
    @Schema(description = "학과명", nullable = true, example = "컴퓨터공학과")
    private final String majorName;
    @Schema(description = "단과대명", nullable = true, example = "공과대학")
    private final String collegeName;
    @Schema(description = "활동 주기", nullable = true, example = "매주 목요일 저녁")
    private final String activityCycle;
    @Schema(description = "회비 유무", example = "false")
    private final boolean hasFee;
    @Schema(description = "공개 여부", example = "true")
    private final boolean isPublic;
    @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
    private final boolean isLiked;
    @Schema(description = "좋아요 수", example = "12")
    private final int likeCount;
    @Schema(description = "정규화된 태그 목록", example = "[\"개발\", \"스터디\", \"커뮤니티\"]")
    private final List<String> tags;
    @Schema(description = "카테고리 id", nullable = true, example = "2")
    private final Long categoryId;
    @Schema(description = "카테고리명", nullable = true, example = "IT")
    private final String categoryName;
    @Schema(description = "작성자 이름", example = "김학생")
    private final String authorName;
    @Schema(description = "생성 시각", example = "2026-03-16T09:00:00")
    private final LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-03-16T10:30:00")
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
