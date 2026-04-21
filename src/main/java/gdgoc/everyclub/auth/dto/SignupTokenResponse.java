package gdgoc.everyclub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 진행용 토큰 응답")
public record SignupTokenResponse(
        @Schema(description = "회원가입 요청에 사용할 임시 토큰", example = "signup-token-example")
        String signupToken,

        @Schema(description = "토큰 만료까지 남은 시간(초)", example = "600")
        long expiresIn
) {
}
