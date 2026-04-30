package gdgoc.everyclub.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT 로그인 토큰 응답")
public class TokenInfo {

    @Schema(description = "토큰 타입", example = "Bearer")
    private String grantType;

    @Schema(description = "JWT access token", example = "jwt-access-token-example")
    private String accessToken;

    @Schema(description = "access token 만료까지 남은 시간(초)", example = "7200")
    private Long expiresIn;
}
