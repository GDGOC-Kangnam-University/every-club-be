package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "동아리 목록 응답용 요약 정보")
public class ClubSummaryResponse {
    @Schema(description = "동아리 id", example = "1")
    private final Long id;
    @Schema(description = "동아리 slug", example = "gdgoc-knu")
    private final String slug;
    @Schema(description = "동아리 이름", example = "강남대 GDGoC")
    private final String name;
    @Schema(description = "목록에 노출되는 한 줄 소개", example = "개발 스터디와 프로젝트를 함께하는 동아리입니다.")
    private final String summary;
    @Schema(description = "로고 이미지 URL", nullable = true, example = "https://cdn.everyclub.app/clubs/gdgoc/logo.png")
    private final String logoUrl;
    @Schema(description = "모집 상태", example = "OPEN")
    private final RecruitingStatus recruitingStatus;
    @Schema(description = "활동 주기", nullable = true, example = "매주 목요일 저녁")
    private final String activityCycle;
    @Schema(description = "회비 유무", example = "false")
    private final boolean hasFee;
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
        this.tags = club.getTagNames();
        this.categoryId = club.getCategory() != null ? club.getCategory().getId() : null;
        this.categoryName = club.getCategory() != null ? club.getCategory().getName() : null;
        this.authorName = club.getAuthor() != null ? club.getAuthor().getName() : "Unknown";
        this.createdAt = club.getCreatedAt();
        this.updatedAt = club.getUpdatedAt();
    }
}
