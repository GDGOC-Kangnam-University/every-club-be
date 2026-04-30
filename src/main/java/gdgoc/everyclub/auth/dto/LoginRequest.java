package gdgoc.everyclub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "로그인 이메일", example = "student@kangnam.ac.kr")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "비밀번호", example = "P@ssw0rd123!")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
