package gdgoc.everyclub.security.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenInfoTest {

    @Test
    @DisplayName("Builder로 생성한 TokenInfo의 필드가 정상 반환된다")
    void builder_and_getters() {
        TokenInfo tokenInfo = TokenInfo.builder()
                .grantType("Bearer")
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .expiresIn(1800L)
                .build();

        assertThat(tokenInfo.getGrantType()).isEqualTo("Bearer");
        assertThat(tokenInfo.getAccessToken()).isEqualTo("test-access-token");
        assertThat(tokenInfo.getRefreshToken()).isEqualTo("test-refresh-token");
        assertThat(tokenInfo.getExpiresIn()).isEqualTo(1800L);
    }

    @Test
    @DisplayName("NoArgsConstructor로 생성하면 모든 필드가 null이다")
    void noArgsConstructor_fieldsAreNull() {
        TokenInfo tokenInfo = new TokenInfo();

        assertThat(tokenInfo.getGrantType()).isNull();
        assertThat(tokenInfo.getAccessToken()).isNull();
        assertThat(tokenInfo.getRefreshToken()).isNull();
        assertThat(tokenInfo.getExpiresIn()).isNull();
    }
}