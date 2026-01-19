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
@RequestMapping("/posts")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    public ApiResponse<Long> createPost(@RequestBody @Valid ClubCreateRequest request) {
        Long id = clubService.createPost(request);
        return ApiResponse.success(id);
    }

    @GetMapping
    public ApiResponse<Page<ClubResponse>> getPosts(Pageable pageable) {
        Page<ClubResponse> responses = clubService.getPosts(pageable)
                .map(ClubResponse::new);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<ClubResponse> getPost(@PathVariable Long id) {
        ClubResponse response = new ClubResponse(clubService.getPostById(id));
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updatePost(@PathVariable Long id, @RequestBody @Valid ClubUpdateRequest request) {
        clubService.updatePost(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        clubService.deletePost(id);
        return ApiResponse.success();
    }
}
