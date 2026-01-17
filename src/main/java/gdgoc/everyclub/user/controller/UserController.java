package gdgoc.everyclub.user.controller;

import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserResponse;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<Long> createUser(@RequestBody @Valid UserCreateRequest request) {
        Long id = userService.createUser(request);
        return ApiResponse.success(id);
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        List<UserResponse> responses = userService.getUsers().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        UserResponse response = new UserResponse(userService.getUserById(id));
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }
}
