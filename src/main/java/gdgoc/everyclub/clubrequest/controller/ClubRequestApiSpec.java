package gdgoc.everyclub.clubrequest.controller;

import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestAdminResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestMyResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestRejectRequest;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.ClubRequestDocs;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.UUID;

@Tag(name = ClubRequestDocs.TAG_NAME, description = ClubRequestDocs.TAG_DESCRIPTION)
public interface ClubRequestApiSpec {

    @PostMapping
    @Operation(summary = "동아리 승인 요청 생성", description = "검토를 위한 동아리 승인 요청을 등록합니다.")
    @RequestBody(
            required = true,
            description = "동아리 승인 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "동아리 승인 요청 예시",
                            value = OpenApiExamples.CREATE_CLUB_REQUEST_REVIEW
                    )
            )
    )
    ApiResponse<ClubRegistrationResponse> createClubRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            ClubRegistrationRequest request);

    @GetMapping
    @Operation(summary = "동아리 승인 요청 목록 조회", description = "관리자 검토용 동아리 승인 요청 목록을 조회합니다.")
    ApiResponse<List<ClubRequestAdminResponse>> getClubRequests();

    @GetMapping("/me")
    @Operation(summary = "내 동아리 등록 신청 목록", description = "내가 신청한 동아리 등록 요청 목록을 최신순으로 반환합니다.")
    ApiResponse<List<ClubRequestMyResponse>> getMyRequests(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @PutMapping("/me/{publicId}")
    @Operation(summary = "동아리 등록 신청 수정 및 재신청", description = "반려된(REJECTED) 신청을 수정하여 재신청합니다.")
    @RequestBody(
            required = true,
            description = "동아리 등록 신청 수정 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "동아리 등록 신청 수정 예시",
                            value = OpenApiExamples.CREATE_CLUB_REQUEST_REVIEW
                    )
            )
    )
    ApiResponse<ClubRegistrationResponse> resubmitClubRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "동아리 요청 공개 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID publicId,
            ClubRegistrationRequest request);

    @GetMapping("/{publicId}")
    @Operation(summary = "동아리 승인 요청 상세 조회", description = "public id로 단건 승인 요청을 조회합니다.")
    ApiResponse<ClubRequestAdminResponse> getClubRequest(
            @Parameter(description = "동아리 요청 공개 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID publicId);

    @PatchMapping("/{publicId}/approve")
    @Operation(summary = "동아리 승인 요청 승인", description = "대기 중인 동아리 승인 요청을 승인하고 실제 동아리를 생성합니다.")
    ApiResponse<ClubRegistrationResponse> approveClubRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "동아리 요청 공개 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID publicId);

    @PatchMapping("/{publicId}/reject")
    @Operation(summary = "동아리 승인 요청 반려", description = "대기 중인 동아리 승인 요청을 관리자 메모와 함께 반려합니다.")
    @RequestBody(
            required = true,
            description = "반려 사유 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "반려 사유 예시",
                            value = OpenApiExamples.REJECT_CLUB_REQUEST
                    )
            )
    )
    ApiResponse<ClubRegistrationResponse> rejectClubRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "동아리 요청 공개 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID publicId,
            ClubRequestRejectRequest request);
}
