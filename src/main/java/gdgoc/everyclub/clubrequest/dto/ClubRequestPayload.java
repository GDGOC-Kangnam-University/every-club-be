package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "동아리 승인 요청에 저장된 요청 스냅샷")
public record ClubRequestPayload(
        @Schema(description = "요청한 동아리 이름", example = "스타트업 빌더스")
        String name,
        @Schema(description = "요청한 카테고리 id", example = "4")
        Long categoryId,
        @Schema(description = "요청한 slug", example = "startup-builders")
        String slug,
        @Schema(description = "검토용 한 줄 소개", example = "창업 워크숍과 팀 프로젝트 중심의 동아리 신청 예시입니다.")
        String summary,
        @Schema(description = "검토용 상세 소개", example = "창업가 초청 세션, 피칭 연습, 팀 매칭 프로그램을 운영하려고 합니다.")
        String description,
        @Schema(description = "로고 이미지 URL", example = "https://cdn.everyclub.app/requests/startup/logo.png")
        String logoUrl,
        @Schema(description = "배너 이미지 URL", example = "https://cdn.everyclub.app/requests/startup/banner.png")
        String bannerUrl,
        @Schema(description = "가입 신청 폼 URL", example = "https://forms.gle/startup-builders")
        String joinFormUrl,
        @Schema(description = "요청한 모집 상태", example = "OPEN")
        RecruitingStatus recruitingStatus,
        @Schema(description = "요청한 학과 id", example = "101", nullable = true)
        Long majorId,
        @Schema(description = "요청한 활동 주기", example = "격주 금요일")
        String activityCycle,
        @Schema(description = "회비 유무", example = "true")
        boolean hasFee,
        @Schema(description = "공개 여부", example = "false")
        boolean isPublic,
        @Schema(description = "요청한 태그 이름 목록", example = "[\"개발\", \"스터디\", \"친목\"]")
        List<String> tags
) {
}
