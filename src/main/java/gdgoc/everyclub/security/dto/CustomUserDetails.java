package gdgoc.everyclub.security.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails 구현체
 * JWT 토큰에 사용자 DB PK와 role을 담기 위한 커스텀 구현
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;      // 사용자 DB PK
    private final String username;  // 로그인 ID or 이메일
    private final String password;
    private final String role;      // ROLE_USER, ROLE_ADMIN 등

    public CustomUserDetails(Long userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}