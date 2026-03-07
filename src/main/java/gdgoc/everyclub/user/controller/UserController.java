package gdgoc.everyclub.user.controller;

import gdgoc.everyclub.auth.EmailService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.user.dto.UserResponse;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    /**
     * Get current authenticated user's profile
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = new UserResponse(userService.getUserById(userDetails.getUserId()));
        return ApiResponse.success(response);
    }

    /**
     * Update current authenticated user's profile
     */
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserUpdateRequest request) {
        userService.updateUser(userDetails.getUserId(), request);
        UserResponse response = new UserResponse(userService.getUserById(userDetails.getUserId()));
        return ApiResponse.success(response);
    }

    /**
     * Delete current authenticated user's account
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getUserId());
        return ApiResponse.success();
    }

    @PostMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestBody String email) {
        return ApiResponse.success(emailService.isSchoolEmail(email));
    }
}
