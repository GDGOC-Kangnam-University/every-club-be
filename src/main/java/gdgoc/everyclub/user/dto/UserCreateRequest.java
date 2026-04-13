package gdgoc.everyclub.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "회원가입 토큰을 입력해주세요.")
        String signupToken,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 10, message = "닉네임은 10자 이하여야 합니다.")
        String nickname,

        @Size(max = 9, message = "학번은 9자 이하여야 합니다.")
        String studentId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        String passwordConfirm
) {
}