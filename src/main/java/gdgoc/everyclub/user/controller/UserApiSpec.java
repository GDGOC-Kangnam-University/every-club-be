package gdgoc.everyclub.user.controller;

import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.docs.UserDocs;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.user.dto.CheckEmailRequest;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserResponse;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = UserDocs.TAG_NAME, description = UserDocs.TAG_DESCRIPTION)
public interface UserApiSpec {

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새 사용자 계정을 생성합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"))
    ApiResponse<UserResponse> signup(UserCreateRequest request);

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "현재 인증된 사용자의 프로필을 반환합니다.")
    ApiResponse<UserResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @PatchMapping("/me")
    @Operation(summary = "내 프로필 수정", description = "현재 인증된 사용자의 프로필을 수정합니다.")
    @RequestBody(
            required = true,
            description = "사용자 프로필 수정 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "프로필 수정 예시",
                            value = OpenApiExamples.USER_UPDATE_REQUEST
                    )
            )
    )
    ApiResponse<UserResponse> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            UserUpdateRequest request);

    @DeleteMapping("/me")
    @Operation(summary = "계정 탈퇴", description = "현재 인증된 사용자의 계정을 삭제합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "계정 삭제 성공"))
    ApiResponse<Void> deleteMyAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @PostMapping("/check-email")
    @Operation(summary = "학교 이메일 확인", description = "입력한 이메일이 학교 이메일인지 확인합니다.")
    @RequestBody(
            required = true,
            description = "학교 메일 여부 확인 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "학교 메일 확인 예시",
                            value = OpenApiExamples.CHECK_EMAIL_REQUEST
                    )
            )
    )
    ApiResponse<Boolean> checkEmail(CheckEmailRequest request);
}
