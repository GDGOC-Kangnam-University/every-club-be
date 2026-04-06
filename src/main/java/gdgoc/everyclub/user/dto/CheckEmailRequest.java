package gdgoc.everyclub.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "학교 메일 여부 확인 요청")
public record CheckEmailRequest(
        @Schema(description = "확인할 이메일", example = "student@kangnam.ac.kr")
        @NotBlank
        @Email
        String email
) {
}
