package gdgoc.everyclub.user.controller;

import gdgoc.everyclub.auth.EmailService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.user.dto.CheckEmailRequest;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserResponse;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and signup APIs")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sign up", description = "Create a new user account.")
    public ApiResponse<UserResponse> signup(@RequestBody @Valid UserCreateRequest request) {
        Long userId = userService.createUser(request);
        UserResponse response = new UserResponse(userService.getUserById(userId));
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Get the current authenticated user's profile.")
    public ApiResponse<UserResponse> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = new UserResponse(userService.getUserById(userDetails.getUserId()));
        return ApiResponse.success(response);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update my profile", description = "Update the current authenticated user's profile.")
    public ApiResponse<UserResponse> updateMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserUpdateRequest request) {
        userService.updateUser(userDetails.getUserId(), request);
        UserResponse response = new UserResponse(userService.getUserById(userDetails.getUserId()));
        return ApiResponse.success(response);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete my account", description = "Delete the current authenticated user's account.")
    public ApiResponse<Void> deleteMyAccount(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getUserId());
        return ApiResponse.success();
    }

    @PostMapping("/check-email")
    @Operation(summary = "Check school email", description = "Check whether the given email is a school email.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "학교 메일 여부 확인 요청 본문",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "학교 메일 확인 예시",
                            value = OpenApiExamples.CHECK_EMAIL_REQUEST
                    )
            )
    )
    public ApiResponse<Boolean> checkEmail(@RequestBody @Valid CheckEmailRequest request) {
        return ApiResponse.success(emailService.isSchoolEmail(request.email()));
    }
}
