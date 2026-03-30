package gdgoc.everyclub.club.controller;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.*;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.common.exception.AuthErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubs", description = "동아리 API")
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    @Operation(summary = "동아리 생성", description = "동아리를 생성하고 생성된 동아리 id를 반환합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "동아리 생성 요청 본문",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "동아리 생성 예시",
                            value = OpenApiExamples.CREATE_CLUB_REQUEST
                    )
            )
    )
    public ApiResponse<Long> createClub(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ClubCreateRequest request) {
        Long id = clubService.createClub(request, userDetails.getUserId());
        return ApiResponse.success(id);
    }

    /**
     * 동아리 목록 조회 (필터 선택적).
     *
     * <p>모든 파라미터는 선택적이다. 파라미터가 없으면 전체 공개 동아리를 반환한다.
     *
     * <ul>
     *   <li>{@code categoryIds} — 동아리 유형 ID (복수 선택).
     *       쉼표 구분({@code ?categoryIds=1,2}) 또는 반복 파라미터({@code ?categoryIds=1&categoryIds=2}) 모두 허용.</li>
     *   <li>{@code collegeId} — 단과대학 ID. major가 없는 동아리(중앙/기타)는 자동 제외.</li>
     *   <li>{@code hasFee} — 회비 유무 ({@code true}/{@code false}).</li>
     *   <li>{@code hasActivity} — 정기 모임 유무. activityCycle 값 존재 여부로 판단.</li>
     * </ul>
     */
    @GetMapping
    @Operation(summary = "동아리 목록 조회", description = "카테고리, 단과대, 활동 여부, 이름, 태그 조건으로 동아리 목록을 조회합니다.")
    public ApiResponse<Page<ClubSummaryResponse>> getClubs(
            @Parameter(description = "필터링할 카테고리 id 목록입니다. 반복 파라미터 또는 쉼표 구분을 사용할 수 있습니다.", example = "1,2")
            @RequestParam(required = false) List<Long> categoryIds,
            @Parameter(description = "단과대 id 필터", example = "1")
            @RequestParam(required = false) Long collegeId,
            @Parameter(description = "회비 유무 필터", example = "false")
            @RequestParam(required = false) Boolean hasFee,
            @Parameter(description = "정기 활동 유무 필터", example = "true")
            @RequestParam(required = false) Boolean hasActivity,
            @Parameter(description = "동아리 이름 검색어", example = "개발")
            @RequestParam(required = false) String name,
            @Parameter(description = "태그 검색어", example = "스터디")
            @RequestParam(required = false) String tag,
            @ParameterObject Pageable pageable
    ) {
        ClubFilterRequest filter = new ClubFilterRequest(categoryIds, collegeId, hasFee, hasActivity, name, tag);
        return ApiResponse.success(clubService.filterClubs(filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "동아리 상세 조회", description = "공개 동아리 상세 정보를 조회합니다. 선택적인 레거시 사용자 헤더가 있으면 좋아요 상태를 함께 계산합니다.")
    public ApiResponse<ClubDetailResponse> getClub(
            @Parameter(description = "동아리 id", example = "1")
            @PathVariable Long id,
            @Parameter(description = "좋아요 상태 계산에 사용하는 레거시 사용자 id 헤더", example = "42", required = false, deprecated = true)
            @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        ClubDetailResponse response = clubService.getPublicClubById(id, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    @Operation(summary = "동아리 검색", description = "이름, 태그 또는 두 조건을 함께 사용해 동아리를 검색합니다. name 또는 tag 중 하나는 반드시 필요합니다.")
    public ApiResponse<Page<ClubSummaryResponse>> searchClubs(
            @Parameter(description = "동아리 이름 검색어", example = "로봇")
            @RequestParam(required = false) String name,
            @Parameter(description = "태그 검색어", example = "ai")
            @RequestParam(required = false) String tag,
            @ParameterObject Pageable pageable) {
        if (name == null && tag == null) {
            throw new LogicException(ValidationErrorCode.INVALID_INPUT);
        }
        ClubFilterRequest filter = new ClubFilterRequest(null, null, null, null, name, tag);
        return ApiResponse.success(clubService.filterClubs(filter, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "동아리 수정", description = "기존 동아리 정보를 수정합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "동아리 수정 요청 본문",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "동아리 수정 예시",
                            value = OpenApiExamples.UPDATE_CLUB_REQUEST
                    )
            )
    )
    public ApiResponse<Void> updateClub(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ClubUpdateRequest request) {
        clubService.updateClub(id, userDetails.getUserId(), request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "동아리 삭제", description = "id로 동아리를 삭제합니다.")
    public ApiResponse<Void> deleteClub(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        clubService.deleteClub(id, userDetails.getUserId());
        return ApiResponse.success();
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "동아리 좋아요 토글", description = "현재 사용자의 동아리 좋아요 상태를 토글하고 변경된 상태를 반환합니다.")
    public ApiResponse<Boolean> toggleLike(
            @Parameter(description = "동아리 id", example = "1")
            @PathVariable @Positive(message = "Club ID must be positive") Long id,
            @Parameter(description = "현재 구현에서 사용하는 레거시 사용자 id 헤더", example = "42", deprecated = true)
            @RequestHeader(name = "X-User-Id") @Positive(message = "User ID must be positive") Long userId
    ) {
        // Authentication check: Validate that the user exists in the system
        // TODO: Replace with @AuthenticationPrincipal after implementing Spring Security
        if (!clubService.validateUserExists(userId)) {
            throw new LogicException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }
        boolean isLiked = clubService.toggleLike(id, userId);
        return ApiResponse.success(isLiked);
    }
}
