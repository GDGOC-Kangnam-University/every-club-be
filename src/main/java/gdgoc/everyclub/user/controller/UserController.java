package gdgoc.everyclub.user.controller;

import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserResponse;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequest request) {
        UUID publicId = userService.createUser(request);
        return ResponseEntity.created(URI.create("/users/" + publicId)).build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> responses = userService.getUsers().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID publicId) {
        UserResponse response = new UserResponse(userService.getUserByPublicId(publicId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<Void> updateUser(@PathVariable UUID publicId, @RequestBody UserUpdateRequest request) {
        userService.updateUser(publicId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID publicId) {
        userService.deleteUser(publicId);
        return ResponseEntity.noContent().build();
    }
}
