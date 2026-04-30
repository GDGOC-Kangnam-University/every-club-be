package gdgoc.everyclub.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record UserCreateRequest(
        @Schema(description = "OTP 검증 후 발급받은 회원가입 토큰", example = "signup-token-example")
        @NotBlank(message = "회원가입 토큰을 입력해주세요.")
        String signupToken,

        @Schema(description = "닉네임. 최대 10자", example = "동아리운영자")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 10, message = "닉네임은 10자 이하여야 합니다.")
        String nickname,

        @Schema(description = "학번. 최대 9자", example = "202401234")
        @Size(max = 9, message = "학번은 9자 이하여야 합니다.")
        String studentId,

        @Schema(description = "비밀번호. 최소 8자", example = "P@ssw0rd123!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @Schema(description = "비밀번호 확인", example = "P@ssw0rd123!")
        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        String passwordConfirm
) {
}
