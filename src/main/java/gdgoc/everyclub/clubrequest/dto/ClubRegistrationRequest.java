package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "동아리 승인 요청 생성 본문")
public record ClubRegistrationRequest(
        @Schema(description = "요청한 동아리 이름", example = "스타트업 빌더스")
        @NotBlank(message = "동아리 이름은 필수입니다.")
        @Size(max = 20, message = "동아리 이름은 20자 이하여야 합니다.")
        String name,

        @Schema(description = "요청한 카테고리 id", example = "4")
        @NotNull(message = "카테고리 id는 필수입니다.")
        Long categoryId,

        @Schema(description = "요청한 slug", example = "startup-builders")
        @NotBlank(message = "slug는 필수입니다.")
        @Size(max = 100, message = "slug는 100자 이하여야 합니다.")
        String slug,

        @Schema(description = "검토용 한 줄 소개", example = "창업 워크숍과 팀 프로젝트 중심의 동아리 신청 예시입니다.")
        @NotBlank(message = "한 줄 소개는 필수입니다.")
        @Size(max = 200, message = "한 줄 소개는 200자 이하여야 합니다.")
        String summary,

        @Schema(description = "검토용 상세 소개", example = "창업가 초청 세션, 피칭 연습, 팀 매칭 프로그램을 운영하려고 합니다.")
        String description,

        @Schema(description = "로고 이미지 URL", example = "https://cdn.everyclub.app/requests/startup/logo.png")
        @Size(max = 2048)
        String logoUrl,

        @Schema(description = "배너 이미지 URL", example = "https://cdn.everyclub.app/requests/startup/banner.png")
        @Size(max = 2048)
        String bannerUrl,

        @Schema(description = "가입 신청 폼 URL", example = "https://forms.gle/startup-builders")
        @Size(max = 2048)
        String joinFormUrl,

        @Schema(description = "요청한 모집 상태", example = "OPEN")
        @NotNull(message = "모집 상태는 필수입니다.")
        RecruitingStatus recruitingStatus,

        @Schema(description = "학과 소속 동아리인 경우의 학과 id", example = "101", nullable = true)
        Long majorId,

        @Schema(description = "활동 주기", example = "격주 금요일")
        @Size(max = 50)
        String activityCycle,

        @Schema(description = "회비 유무", example = "true")
        boolean hasFee,

        @Schema(description = "공개 여부", example = "false")
        boolean isPublic,

        @Schema(description = "요청한 태그 이름 목록", example = "[\"개발\", \"스터디\", \"친목\"]")
        @Size(max = 20, message = "태그는 최대 20개까지 입력할 수 있습니다.")
        List<String> tags
) {
}
