package gdgoc.everyclub.user.dto;

public record UserUpdateRequest(
        String nickname,
        String department,
        String studentId,
        String phoneNumber,
        String bio
) {
}
