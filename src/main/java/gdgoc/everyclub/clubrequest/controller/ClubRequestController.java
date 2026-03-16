package gdgoc.everyclub.clubrequest.controller;

import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestAdminResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestRejectRequest;
import gdgoc.everyclub.clubrequest.service.ClubRequestService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/club-requests")
@RequiredArgsConstructor
public class ClubRequestController {

    private final ClubRequestService clubRequestService;

    @PostMapping
    public ApiResponse<ClubRegistrationResponse> createClubRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ClubRegistrationRequest request) {
        ClubRegistrationResponse response = clubRequestService.createClubRequest(userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<ClubRequestAdminResponse>> getClubRequests() {
        return ApiResponse.success(clubRequestService.getClubRequests());
    }

    @GetMapping("/{publicId}")
    public ApiResponse<ClubRequestAdminResponse> getClubRequest(@PathVariable UUID publicId) {
        return ApiResponse.success(clubRequestService.getClubRequest(publicId));
    }

    @PatchMapping("/{publicId}/approve")
    public ApiResponse<ClubRegistrationResponse> approveClubRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId) {
        ClubRegistrationResponse response = clubRequestService.approveClubRequest(publicId, userDetails.getUserId());
        return ApiResponse.success(response);
    }

    @PatchMapping("/{publicId}/reject")
    public ApiResponse<ClubRegistrationResponse> rejectClubRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid ClubRequestRejectRequest request) {
        ClubRegistrationResponse response = clubRequestService.rejectClubRequest(publicId, userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }
}