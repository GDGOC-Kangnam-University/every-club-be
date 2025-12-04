package gdgoc.everyclub.user.controller;

import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserResponse;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody UserCreateRequest request) {
        Long id = userService.createUser(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> responses = userService.getUsers().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse response = new UserResponse(userService.getUserById(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
