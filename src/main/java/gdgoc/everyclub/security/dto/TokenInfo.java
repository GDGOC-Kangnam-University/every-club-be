package gdgoc.everyclub.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// JWT 토큰 응답 DTO
 
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {

    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}