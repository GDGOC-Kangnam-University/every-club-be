package gdgoc.everyclub.club.controller;

import gdgoc.everyclub.club.dto.ClubCreateRequest;
import gdgoc.everyclub.club.dto.ClubResponse;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.common.ApiResponse;
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
    public ApiResponse<Page<ClubResponse>> getClubs(Pageable pageable) {
        Page<ClubResponse> responses = clubService.getClubs(pageable)
                .map(ClubResponse::new);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<ClubResponse> getClub(@PathVariable Long id) {
        ClubResponse response = new ClubResponse(clubService.getClubById(id));
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
}
