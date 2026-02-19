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
        Page<ClubSummaryResponse> responses = clubService.getClubs(pageable)
                .map(ClubSummaryResponse::new);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<ClubDetailResponse> getClub(@PathVariable Long id) {
        ClubDetailResponse response = clubService.getPublicClubById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ClubSummaryResponse>> searchClubsByTag(
            @RequestParam String tag,
            Pageable pageable) {
        Page<ClubSummaryResponse> responses = clubService.searchClubsByTag(tag, pageable)
                .map(ClubSummaryResponse::new);
        return ApiResponse.success(responses);
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
}
