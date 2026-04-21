package gdgoc.everyclub.club.controller;

import gdgoc.everyclub.club.dto.AddClubAdminRequest;
import gdgoc.everyclub.club.dto.ClubAdminResponse;
import gdgoc.everyclub.club.dto.ClubDetailResponse;
import gdgoc.everyclub.club.dto.ClubSummaryResponse;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.dto.DelegateClubAdminRequest;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.ClubDocs;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = ClubDocs.TAG_NAME, description = ClubDocs.TAG_DESCRIPTION)
public interface ClubApiSpec {

    @GetMapping("/me")
    @Operation(summary = "내가 관리하는 동아리 목록", description = "ClubAdmin에 등록된 내 동아리 목록을 반환합니다. 비공개 동아리도 포함됩니다.")
    ApiResponse<List<ClubSummaryResponse>> getManagedClubs(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @GetMapping("/liked")
    @Operation(summary = "좋아요한 동아리 목록", description = "내가 좋아요한 공개 동아리 목록을 반환합니다.")
    ApiResponse<Page<ClubSummaryResponse>> getLikedClubs(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject Pageable pageable);

    @GetMapping("/{id}/admins")
    @Operation(summary = "동아리 관리자 목록", description = "해당 동아리의 관리자 목록(LEAD/MEMBER)을 반환합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "동아리 관리자 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "관리자 목록 응답 예시",
                            value = OpenApiExamples.CLUB_ADMIN_LIST_RESPONSE
                    )
            )
    ))
    ApiResponse<List<ClubAdminResponse>> getClubAdmins(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1") @PathVariable Long id);

    @PostMapping("/{id}/admins")
    @Operation(summary = "동아리 관리자 추가", description = "동아리에 관리자를 MEMBER 역할로 추가합니다. LEAD만 가능합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "동아리 관리자 추가 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "관리자 추가 성공 예시",
                            value = OpenApiExamples.SUCCESS_VOID_RESPONSE
                    )
            )
    ))
    @RequestBody(
            required = true,
            description = "관리자 추가 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "관리자 추가 예시",
                            value = OpenApiExamples.ADD_CLUB_ADMIN_REQUEST
                    )
            )
    )
    ApiResponse<Void> addClubAdmin(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1") @PathVariable Long id,
            AddClubAdminRequest request);

    @DeleteMapping("/{id}/admins/{userId}")
    @Operation(summary = "동아리 관리자 제거", description = "동아리 관리자를 제거합니다. LEAD만 가능하며, 마지막 관리자는 제거할 수 없습니다.")
    ApiResponse<Void> removeClubAdmin(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1") @PathVariable Long id,
            @Parameter(description = "제거할 사용자 id", example = "42") @PathVariable Long userId);

    @PostMapping("/{id}/admins/delegate")
    @Operation(summary = "동아리 LEAD 위임", description = "LEAD 권한을 다른 관리자(MEMBER)에게 위임합니다. formerLeaderAction으로 기존 LEAD를 MEMBER로 강등(DEMOTE)하거나 제거(REMOVE)를 선택합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "동아리 LEAD 위임 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "LEAD 위임 성공 예시",
                            value = OpenApiExamples.SUCCESS_VOID_RESPONSE
                    )
            )
    ))
    @RequestBody(
            required = true,
            description = "LEAD 위임 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "LEAD 위임 예시",
                            value = OpenApiExamples.DELEGATE_CLUB_ADMIN_REQUEST
                    )
            )
    )
    ApiResponse<Void> delegateClub(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1") @PathVariable Long id,
            DelegateClubAdminRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @GetMapping
    @Operation(summary = "동아리 목록 조회", description = "카테고리, 단과대, 활동 여부, 이름, 태그 조건으로 동아리 목록을 조회합니다.")
    ApiResponse<Page<ClubSummaryResponse>> getClubs(
            @Parameter(description = "필터링할 카테고리 id 목록입니다. 반복 파라미터 또는 쉼표 구분을 사용할 수 있습니다.", example = "1,2")
            @RequestParam(required = false) List<Long> categoryIds,
            @Parameter(description = "단과대 id 필터", example = "1")
            @RequestParam(required = false) Long collegeId,
            @Parameter(description = "회비 유무 필터", example = "false")
            @RequestParam(required = false) Boolean hasFee,
            @Parameter(description = "정기 활동 유무 필터", example = "true")
            @RequestParam(required = false) Boolean hasActivity,
            @Parameter(description = ClubDocs.PARAM_NAME, example = "개발")
            @RequestParam(required = false) String name,
            @Parameter(description = ClubDocs.PARAM_TAG, example = "스터디")
            @RequestParam(required = false) String tag,
            @ParameterObject Pageable pageable);

    @GetMapping("/{id}")
    @Operation(summary = "동아리 상세 조회", description = "공개 동아리 상세 정보를 조회합니다. 인증된 사용자의 좋아요 상태를 함께 반환합니다.")
    ApiResponse<ClubDetailResponse> getClub(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @GetMapping("/search")
    @Operation(summary = "동아리 검색", description = "이름, 태그 또는 두 조건을 함께 사용해 동아리를 검색합니다. name 또는 tag 중 하나는 반드시 필요합니다.")
    ApiResponse<Page<ClubSummaryResponse>> searchClubs(
            @Parameter(description = ClubDocs.PARAM_NAME, example = "로봇")
            @RequestParam(required = false) String name,
            @Parameter(description = ClubDocs.PARAM_TAG, example = "ai")
            @RequestParam(required = false) String tag,
            @ParameterObject Pageable pageable);

    @PutMapping("/{id}")
    @Operation(summary = "동아리 수정", description = "기존 동아리 정보를 수정합니다.")
    @RequestBody(
            required = true,
            description = "동아리 수정 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "동아리 수정 예시",
                            value = OpenApiExamples.UPDATE_CLUB_REQUEST
                    )
            )
    )
    ApiResponse<Void> updateClub(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1") @PathVariable Long id,
            ClubUpdateRequest request);

    @DeleteMapping("/{id}")
    @Operation(summary = "동아리 삭제", description = "id로 동아리를 삭제합니다. LEAD만 가능합니다.")
    ApiResponse<Void> deleteClub(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1") @PathVariable Long id);

    @PostMapping("/{id}/like")
    @Operation(summary = "동아리 좋아요 토글", description = "현재 사용자의 동아리 좋아요 상태를 토글하고 변경된 상태를 반환합니다.")
    ApiResponse<Boolean> toggleLike(
            @Parameter(description = ClubDocs.PARAM_CLUB_ID, example = "1")
            @PathVariable @Positive(message = "Club ID must be positive") Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
