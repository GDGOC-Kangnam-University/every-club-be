package gdgoc.everyclub.club.controller;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.*;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
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

    @GetMapping
    public ApiResponse<Page<ClubSummaryResponse>> getClubs(Pageable pageable) {
        Page<ClubSummaryResponse> responses = clubService.getClubsWithLikeCounts(pageable);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<ClubDetailResponse> getClub(
            @PathVariable Long id,
            @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        ClubDetailResponse response = clubService.getPublicClubById(id, userId);
        return ApiResponse.success(response);
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
            @PathVariable Long id,
            // TODO: Security Vulnerability - Replace with @AuthenticationPrincipal after implementing Spring Security
            @RequestHeader(name = "X-User-Id") Long userId
    ) {
        boolean isLiked = clubService.toggleLike(id, userId);
        return ApiResponse.success(isLiked);
    }
}
