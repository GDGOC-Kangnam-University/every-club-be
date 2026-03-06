package gdgoc.everyclub.security.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    @DisplayName("생성자로 전달한 값이 getter로 정상 반환된다")
    void constructor_and_getters() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "testuser", "password123", "ROLE_USER");

        assertThat(userDetails.getUserId()).isEqualTo(1L);
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password123");
        assertThat(userDetails.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("getAuthorities는 role을 SimpleGrantedAuthority로 반환한다")
    void getAuthorities_returnsRoleAsAuthority() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "user", "pw", "ROLE_ADMIN");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("계정 상태 메서드들은 모두 true를 반환한다")
    void accountStatusMethods_returnTrue() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "user", "pw", "ROLE_USER");

        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }
}