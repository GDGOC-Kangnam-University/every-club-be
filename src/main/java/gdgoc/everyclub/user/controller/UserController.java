package gdgoc.everyclub.user.controller;

import gdgoc.everyclub.auth.EmailService;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.user.dto.CheckEmailRequest;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserResponse;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApiSpec {

    private final UserService userService;
    private final EmailService emailService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@RequestBody @Valid UserCreateRequest request) {
        Long userId = userService.createUser(request);
        UserResponse response = new UserResponse(userService.getUserById(userId));
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<UserResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = new UserResponse(userService.getUserById(userDetails.getUserId()));
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<UserResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserUpdateRequest request) {
        userService.updateUser(userDetails.getUserId(), request);
        UserResponse response = new UserResponse(userService.getUserById(userDetails.getUserId()));
        return ApiResponse.success(response);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getUserId());
        return ApiResponse.success();
    }

    @Override
    public ApiResponse<Boolean> checkEmail(@RequestBody @Valid CheckEmailRequest request) {
        return ApiResponse.success(emailService.isSchoolEmail(request.email()));
    }
}
