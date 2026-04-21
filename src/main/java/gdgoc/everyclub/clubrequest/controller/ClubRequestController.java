package gdgoc.everyclub.clubrequest.controller;

import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestAdminResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestMyResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestRejectRequest;
import gdgoc.everyclub.clubrequest.service.ClubRequestService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/club-requests")
@RequiredArgsConstructor
public class ClubRequestController implements ClubRequestApiSpec {

    private final ClubRequestService clubRequestService;

    @Override
    public ApiResponse<ClubRegistrationResponse> createClubRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ClubRegistrationRequest request) {
        ClubRegistrationResponse response = clubRequestService.createClubRequest(userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @Override
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ApiResponse<List<ClubRequestAdminResponse>> getClubRequests() {
        return ApiResponse.success(clubRequestService.getClubRequests());
    }

    @Override
    public ApiResponse<List<ClubRequestMyResponse>> getMyRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(clubRequestService.getMyRequests(userDetails.getUserId()));
    }

    @Override
    public ApiResponse<ClubRegistrationResponse> resubmitClubRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid ClubRegistrationRequest request) {
        ClubRegistrationResponse response = clubRequestService.resubmitClubRequest(publicId, userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @Override
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ApiResponse<ClubRequestAdminResponse> getClubRequest(@PathVariable UUID publicId) {
        return ApiResponse.success(clubRequestService.getClubRequest(publicId));
    }

    @Override
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ApiResponse<ClubRegistrationResponse> approveClubRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId) {
        ClubRegistrationResponse response = clubRequestService.approveClubRequest(publicId, userDetails.getUserId());
        return ApiResponse.success(response);
    }

    @Override
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ApiResponse<ClubRegistrationResponse> rejectClubRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid ClubRequestRejectRequest request) {
        ClubRegistrationResponse response = clubRequestService.rejectClubRequest(publicId, userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }
}
