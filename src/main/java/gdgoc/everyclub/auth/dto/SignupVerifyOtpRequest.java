package gdgoc.everyclub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 OTP 검증 요청")
public record SignupVerifyOtpRequest(
        @Schema(description = "OTP를 발급받은 학교 이메일", example = "student@kangnam.ac.kr")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "이메일로 받은 6자리 OTP 코드", example = "123456")
        @NotBlank(message = "OTP 코드를 입력해주세요.")
        String code
) {
}
