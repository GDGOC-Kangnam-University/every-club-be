package gdgoc.everyclub.club.controller;

import gdgoc.everyclub.club.dto.AddClubAdminRequest;
import gdgoc.everyclub.club.dto.ClubAdminResponse;
import gdgoc.everyclub.club.dto.ClubDetailResponse;
import gdgoc.everyclub.club.dto.ClubFilterRequest;
import gdgoc.everyclub.club.dto.ClubSummaryResponse;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.dto.DelegateClubAdminRequest;
import gdgoc.everyclub.club.service.ClubAdminService;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Validated
public class ClubController implements ClubApiSpec {

    private final ClubService clubService;
    private final ClubAdminService clubAdminService;

    @Override
    public ApiResponse<List<ClubSummaryResponse>> getManagedClubs(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(clubAdminService.getManagedClubs(userDetails.getUserId()));
    }

    @Override
    public ApiResponse<Page<ClubSummaryResponse>> getLikedClubs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        return ApiResponse.success(clubAdminService.getLikedClubs(userDetails.getUserId(), pageable));
    }

    @Override
    @PreAuthorize("@clubAdminGuard.canManage(authentication, #id)")
    public ApiResponse<List<ClubAdminResponse>> getClubAdmins(@PathVariable Long id) {
        return ApiResponse.success(clubAdminService.getClubAdmins(id));
    }

    @Override
    @PreAuthorize("@clubAdminGuard.canLead(authentication, #id)")
    public ApiResponse<Void> addClubAdmin(
            @PathVariable Long id,
            AddClubAdminRequest request) {
        clubAdminService.addClubAdmin(id, request.userId());
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("@clubAdminGuard.canLead(authentication, #id)")
    public ApiResponse<Void> removeClubAdmin(
            @PathVariable Long id,
            @PathVariable Long userId) {
        clubAdminService.removeClubAdmin(id, userId);
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("@clubAdminGuard.canDelegate(authentication, #id)")
    public ApiResponse<Void> delegateClub(
            @PathVariable Long id,
            DelegateClubAdminRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        clubAdminService.delegateClub(id, userDetails.getUserId(), request.targetUserId(), request.formerLeaderAction());
        return ApiResponse.success();
    }

    @Override
    public ApiResponse<Page<ClubSummaryResponse>> getClubs(
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Boolean hasFee,
            @RequestParam(required = false) Boolean hasActivity,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tag,
            Pageable pageable) {
        ClubFilterRequest filter = new ClubFilterRequest(categoryIds, collegeId, hasFee, hasActivity, name, tag);
        return ApiResponse.success(clubService.filterClubs(filter, pageable));
    }

    @Override
    public ApiResponse<ClubDetailResponse> getClub(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ClubDetailResponse response = clubService.getPublicClubById(id, userDetails.getUserId());
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<Page<ClubSummaryResponse>> searchClubs(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String tag,
            Pageable pageable) {
        if (name == null && tag == null) {
            throw new LogicException(ValidationErrorCode.INVALID_INPUT);
        }
        ClubFilterRequest filter = new ClubFilterRequest(null, null, null, null, name, tag);
        return ApiResponse.success(clubService.filterClubs(filter, pageable));
    }

    @Override
    @PreAuthorize("@clubAdminGuard.canManage(authentication, #id)")
    public ApiResponse<Void> updateClub(
            @PathVariable Long id,
            ClubUpdateRequest request) {
        clubService.updateClub(id, request);
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("@clubAdminGuard.canLead(authentication, #id)")
    public ApiResponse<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ApiResponse.success();
    }

    @Override
    public ApiResponse<Boolean> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isLiked = clubService.toggleLike(id, userDetails.getUserId());
        return ApiResponse.success(isLiked);
    }
}
