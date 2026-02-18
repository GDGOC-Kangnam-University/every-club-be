package gdgoc.everyclub.security.jwt;

import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.security.dto.TokenInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    // HS512 최소 512bit(64바이트) 키 필요
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "this-is-a-test-secret-key-that-must-be-at-least-64-bytes-long-for-hs512!!".getBytes()
    );
    private static final long EXPIRATION_MINUTES = 30;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(TEST_SECRET, EXPIRATION_MINUTES);
    }

    private Authentication createAuthentication(Long userId, String role) {
        CustomUserDetails userDetails = new CustomUserDetails(userId, "testuser", "password", role);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("Authentication으로부터 토큰을 정상 생성한다")
        void generateToken_success() {
            Authentication authentication = createAuthentication(1L, "ROLE_USER");

            TokenInfo tokenInfo = jwtProvider.generateToken(authentication);

            assertThat(tokenInfo).isNotNull();
            assertThat(tokenInfo.getGrantType()).isEqualTo("Bearer");
            assertThat(tokenInfo.getAccessToken()).isNotBlank();
            assertThat(tokenInfo.getExpiresIn()).isEqualTo(EXPIRATION_MINUTES * 60);
        }

        @Test
        @DisplayName("생성된 토큰에 올바른 subject(userId)가 포함된다")
        void generateToken_containsCorrectUserId() {
            Authentication authentication = createAuthentication(42L, "ROLE_ADMIN");

            TokenInfo tokenInfo = jwtProvider.generateToken(authentication);
            String userId = jwtProvider.getUserId(tokenInfo.getAccessToken());

            assertThat(userId).isEqualTo("42");
        }

        @Test
        @DisplayName("생성된 토큰에 올바른 role 클레임이 포함된다")
        void generateToken_containsCorrectRole() {
            Authentication authentication = createAuthentication(1L, "ROLE_ADMIN");

            TokenInfo tokenInfo = jwtProvider.generateToken(authentication);
            Authentication result = jwtProvider.getAuthentication(tokenInfo.getAccessToken());
            CustomUserDetails principal = (CustomUserDetails) result.getPrincipal();

            assertThat(principal.getRole()).isEqualTo("ROLE_ADMIN");
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("유효한 토큰은 true를 반환한다")
        void validateToken_validToken_returnsTrue() {
            Authentication authentication = createAuthentication(1L, "ROLE_USER");
            String token = jwtProvider.generateToken(authentication).getAccessToken();

            assertThat(jwtProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰은 false를 반환한다")
        void validateToken_expiredToken_returnsFalse() {
            // expiration=0으로 즉시 만료 토큰 생성
            JwtProvider expiredProvider = new JwtProvider(TEST_SECRET, 0);
            Authentication authentication = createAuthentication(1L, "ROLE_USER");
            String token = expiredProvider.generateToken(authentication).getAccessToken();

            assertThat(jwtProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("변조된 토큰은 false를 반환한다")
        void validateToken_tamperedToken_returnsFalse() {
            Authentication authentication = createAuthentication(1L, "ROLE_USER");
            String token = jwtProvider.generateToken(authentication).getAccessToken();
            // 토큰 마지막 문자를 변경하여 서명 위조
            String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

            assertThat(jwtProvider.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("형식이 잘못된 토큰은 false를 반환한다")
        void validateToken_malformedToken_returnsFalse() {
            assertThat(jwtProvider.validateToken("not.a.jwt")).isFalse();
        }

        @Test
        @DisplayName("다른 키로 서명된 토큰은 false를 반환한다")
        void validateToken_differentKey_returnsFalse() {
            String otherSecret = Base64.getEncoder().encodeToString(
                    "another-secret-key-that-is-also-at-least-64-bytes-long-for-hs512-algorithm!!".getBytes()
            );
            JwtProvider otherProvider = new JwtProvider(otherSecret, EXPIRATION_MINUTES);
            Authentication authentication = createAuthentication(1L, "ROLE_USER");
            String token = otherProvider.generateToken(authentication).getAccessToken();

            assertThat(jwtProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 토큰은 false를 반환한다")
        void validateToken_emptyToken_returnsFalse() {
            assertThat(jwtProvider.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("지원되지 않는 JWT 형식(비서명 토큰)은 false를 반환한다")
        void validateToken_unsignedToken_returnsFalse() {
            // 서명 없는 JWT (header.payload.) → UnsupportedJwtException 발생
            String unsignedToken = Jwts.builder()
                    .subject("1")
                    .compact();

            assertThat(jwtProvider.validateToken(unsignedToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("getAuthentication")
    class GetAuthentication {

        @Test
        @DisplayName("유효한 토큰에서 Authentication을 정상 추출한다")
        void getAuthentication_validToken_returnsAuthentication() {
            Authentication authentication = createAuthentication(1L, "ROLE_USER");
            String token = jwtProvider.generateToken(authentication).getAccessToken();

            Authentication result = jwtProvider.getAuthentication(token);

            assertThat(result).isNotNull();
            assertThat(result.getPrincipal()).isInstanceOf(CustomUserDetails.class);

            CustomUserDetails principal = (CustomUserDetails) result.getPrincipal();
            assertThat(principal.getUserId()).isEqualTo(1L);
            assertThat(principal.getRole()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("role 클레임이 없는 토큰은 RuntimeException을 던진다")
        void getAuthentication_noRoleClaim_throwsException() {
            // role 없이 토큰 직접 생성
            SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
            String tokenWithoutRole = Jwts.builder()
                    .subject("1")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(secretKey, Jwts.SIG.HS512)
                    .compact();

            assertThatThrownBy(() -> jwtProvider.getAuthentication(tokenWithoutRole))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("권한 정보가 없는 토큰입니다.");
        }

        @Test
        @DisplayName("만료된 토큰에서도 Authentication을 추출할 수 있다")
        void getAuthentication_expiredToken_stillExtractsClaims() {
            // parseClaims가 ExpiredJwtException에서 claims를 꺼내므로 동작해야 함
            SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
            String expiredToken = Jwts.builder()
                    .subject("99")
                    .claim("role", "ROLE_USER")
                    .issuedAt(new Date(System.currentTimeMillis() - 120000))
                    .expiration(new Date(System.currentTimeMillis() - 60000))
                    .signWith(secretKey, Jwts.SIG.HS512)
                    .compact();

            Authentication result = jwtProvider.getAuthentication(expiredToken);

            CustomUserDetails principal = (CustomUserDetails) result.getPrincipal();
            assertThat(principal.getUserId()).isEqualTo(99L);
            assertThat(principal.getRole()).isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("getUserId")
    class GetUserId {

        @Test
        @DisplayName("토큰에서 userId를 정상 추출한다")
        void getUserId_validToken_returnsUserId() {
            Authentication authentication = createAuthentication(123L, "ROLE_USER");
            String token = jwtProvider.generateToken(authentication).getAccessToken();

            assertThat(jwtProvider.getUserId(token)).isEqualTo("123");
        }

        @Test
        @DisplayName("만료된 토큰에서도 userId를 추출할 수 있다")
        void getUserId_expiredToken_returnsUserId() {
            SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
            String expiredToken = Jwts.builder()
                    .subject("55")
                    .claim("role", "ROLE_USER")
                    .issuedAt(new Date(System.currentTimeMillis() - 120000))
                    .expiration(new Date(System.currentTimeMillis() - 60000))
                    .signWith(secretKey, Jwts.SIG.HS512)
                    .compact();

            assertThat(jwtProvider.getUserId(expiredToken)).isEqualTo("55");
        }
    }
}