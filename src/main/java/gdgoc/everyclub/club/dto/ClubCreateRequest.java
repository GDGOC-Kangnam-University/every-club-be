package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "동아리 생성 요청")
public record ClubCreateRequest(
        @Schema(description = "동아리 이름", example = "강남대 GDGoC")
        @NotBlank(message = "Name must not be empty")
        @Size(max = 20, message = "Name must be less than 20 characters")
        String name,

        @Schema(description = "작성자 사용자 id", example = "1")
        @NotNull(message = "Author ID must not be null")
        Long authorId,

        @Schema(description = "카테고리 id", example = "2")
        @NotNull(message = "Category ID must not be null")
        Long categoryId,

        @Schema(description = "동아리 고유 slug", example = "gdgoc-knu")
        @NotBlank(message = "Slug must not be empty")
        @Size(max = 100, message = "Slug must be less than 100 characters")
        String slug,

        @Schema(description = "목록에 노출되는 한 줄 소개", example = "개발 스터디와 프로젝트를 함께하는 동아리입니다.")
        @NotBlank(message = "Summary must not be empty")
        @Size(max = 200, message = "Summary must be less than 200 characters")
        String summary,

        @Schema(description = "상세 소개", example = "정기 스터디, 연사 세션, 사이드 프로젝트를 함께 진행합니다.")
        String description,

        @Schema(description = "로고 이미지 URL", example = "https://cdn.everyclub.app/clubs/gdgoc/logo.png")
        @Size(max = 2048)
        String logoUrl,

        @Schema(description = "배너 이미지 URL", example = "https://cdn.everyclub.app/clubs/gdgoc/banner.png")
        @Size(max = 2048)
        String bannerUrl,

        @Schema(description = "가입 신청 폼 URL", example = "https://forms.gle/join-gdgoc-knu")
        @Size(max = 2048)
        String joinFormUrl,

        @Schema(description = "모집 상태", example = "OPEN")
        @NotNull(message = "Recruiting status must not be null")
        RecruitingStatus recruitingStatus,

        @Schema(description = "학과 소속 동아리인 경우의 학과 id", example = "101", nullable = true)
        Long majorId,

        @Schema(description = "활동 주기", example = "매주 목요일 저녁")
        @Size(max = 50)
        String activityCycle,

        @Schema(description = "회비 유무", example = "false")
        boolean hasFee,

        @Schema(description = "공개 여부", example = "true")
        boolean isPublic,

        @Schema(description = "태그 목록. 최소 1개, 최대 20개까지 입력할 수 있습니다.", example = "[\"개발\", \"스터디\", \"커뮤니티\"]")
        @NotEmpty(message = "At least one tag is required")
        @Size(max = 20, message = "Maximum 20 tags allowed")
        List<@Size(max = 30, message = "Each tag must be less than 30 characters") String> tags
) {
}
