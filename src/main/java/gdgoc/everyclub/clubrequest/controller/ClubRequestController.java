package gdgoc.everyclub.clubrequest.controller;

import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestAdminResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestRejectRequest;
import gdgoc.everyclub.clubrequest.service.ClubRequestService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/club-requests")
@RequiredArgsConstructor
@Tag(name = "Club Requests", description = "동아리 승인 요청 API")
public class ClubRequestController {

    private final ClubRequestService clubRequestService;

    @PostMapping
    @Operation(summary = "동아리 승인 요청 생성", description = "검토를 위한 동아리 승인 요청을 등록합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "동아리 승인 요청 본문",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "동아리 승인 요청 예시",
                            value = OpenApiExamples.CREATE_CLUB_REQUEST_REVIEW
                    )
            )
    )
    public ApiResponse<ClubRegistrationResponse> createClubRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ClubRegistrationRequest request) {
        ClubRegistrationResponse response = clubRequestService.createClubRequest(userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "동아리 승인 요청 목록 조회", description = "관리자 검토용 동아리 승인 요청 목록을 조회합니다.")
    public ApiResponse<List<ClubRequestAdminResponse>> getClubRequests() {
        return ApiResponse.success(clubRequestService.getClubRequests());
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "동아리 승인 요청 상세 조회", description = "public id로 단건 승인 요청을 조회합니다.")
    public ApiResponse<ClubRequestAdminResponse> getClubRequest(@PathVariable UUID publicId) {
        return ApiResponse.success(clubRequestService.getClubRequest(publicId));
    }

    @PatchMapping("/{publicId}/approve")
    @Operation(summary = "동아리 승인 요청 승인", description = "대기 중인 동아리 승인 요청을 승인하고 실제 동아리를 생성합니다.")
    public ApiResponse<ClubRegistrationResponse> approveClubRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId) {
        ClubRegistrationResponse response = clubRequestService.approveClubRequest(publicId, userDetails.getUserId());
        return ApiResponse.success(response);
    }

    @PatchMapping("/{publicId}/reject")
    @Operation(summary = "동아리 승인 요청 반려", description = "대기 중인 동아리 승인 요청을 관리자 메모와 함께 반려합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "반려 사유 요청 본문",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "반려 사유 예시",
                            value = OpenApiExamples.REJECT_CLUB_REQUEST
                    )
            )
    )
    public ApiResponse<ClubRegistrationResponse> rejectClubRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID publicId,
            @RequestBody @Valid ClubRequestRejectRequest request) {
        ClubRegistrationResponse response = clubRequestService.rejectClubRequest(publicId, userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }
}
