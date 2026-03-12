package gdgoc.everyclub.club.controller;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.*;
import gdgoc.everyclub.club.service.ClubService;
import java.util.List;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.common.exception.AuthErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    public ApiResponse<Long> createClub(@RequestBody @Valid ClubCreateRequest request) {
        Long id = clubService.createClub(request);
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
    public ApiResponse<Page<ClubSummaryResponse>> getClubs(
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Boolean hasFee,
            @RequestParam(required = false) Boolean hasActivity,
            Pageable pageable
    ) {
        ClubFilterRequest filter = new ClubFilterRequest(categoryIds, collegeId, hasFee, hasActivity);
        return ApiResponse.success(clubService.filterClubs(filter, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClubDetailResponse> getClub(
            @PathVariable Long id,
            @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        ClubDetailResponse response = clubService.getPublicClubById(id, userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ClubSummaryResponse>> searchClubs(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tag,
            Pageable pageable) {
        if (name != null) {
            return ApiResponse.success(clubService.searchClubsByName(name, pageable));
        }
        if (tag != null) {
            return ApiResponse.success(
                    clubService.searchClubsByTag(tag, pageable).map(ClubSummaryResponse::new));
        }
        throw new LogicException(ValidationErrorCode.INVALID_INPUT);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateClub(@PathVariable Long id, @RequestBody @Valid ClubUpdateRequest request) {
        clubService.updateClub(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Boolean> toggleLike(
            @PathVariable @Positive(message = "Club ID must be positive") Long id,
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
