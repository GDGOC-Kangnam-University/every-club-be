package gdgoc.everyclub.security.jwt;

import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.security.dto.TokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 Provider
 *
 * 검증 실패 시 예외를 던지지 않고 false를 반환하여
 * 필터에서 깔끔하게 분기 처리할 수 있도록 설계
 */
@Slf4j
@Component
public class JwtProvider {

    private static final String ROLE_KEY = "role";
    private static final String BEARER_TYPE = "Bearer";

    private final SecretKey key;
    private final long tokenValidityInMilliseconds;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long tokenValidityInMinutes) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenValidityInMilliseconds = tokenValidityInMinutes * 60 * 1000;
    }

    /**
     * Authentication 객체로부터 JWT 토큰 생성
     * Payload: { "sub": userId(PK), "role": "ROLE_USER" }
     */
    public TokenInfo generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        long now = System.currentTimeMillis();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        String accessToken = Jwts.builder()
                .subject(String.valueOf(userDetails.getUserId()))  // DB PK
                .claim(ROLE_KEY, userDetails.getRole())            // role
                .issuedAt(new Date(now))
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS512)
                .compact();

        return TokenInfo.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .expiresIn(tokenValidityInMilliseconds / 1000)
                .build();
    }

    /**
     * JWT 토큰 검증
     *
     * @return 유효한 토큰이면 true, 만료/위조/형식 오류 등 모든 실패 케이스에서 false 반환
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * JWT 토큰에서 인증 정보 추출
     * Claims에서 userId(PK)와 role을 추출하여 CustomUserDetails 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String role = claims.get(ROLE_KEY, String.class);
        if (role == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Long userId = Long.parseLong(claims.getSubject());
        CustomUserDetails principal = new CustomUserDetails(userId, "", "", role);

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * 토큰에서 사용자 ID(subject) 추출
     */
    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }
}