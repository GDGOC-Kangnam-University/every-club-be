package gdgoc.everyclub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 OTP 발송 요청")
public record SignupSendOtpRequest(
        @Schema(description = "OTP를 받을 학교 이메일", example = "student@kangnam.ac.kr")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {
}
