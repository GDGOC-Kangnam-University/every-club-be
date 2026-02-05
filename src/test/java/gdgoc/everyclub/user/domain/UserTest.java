package gdgoc.everyclub.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("유저는 생성 시 기본적으로 GUEST 권한을 가진다")
    void userHasGuestRoleByDefault() {
        // given
        String name = "John Doe";
        String email = "john@example.com";

        // when
        User user = new User(name, email);

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    @DisplayName("유저 정보 수정 시 이름은 변경되지만 이메일은 변경되지 않는다")
    void updateOnlyChangesName() {
        // given
        String originalName = "John Doe";
        String email = "john@example.com";
        User user = new User(originalName, email);
        
        String newName = "John Smith";

        // when
        user.update(newName);

        // then
        assertThat(user.getName()).isEqualTo(newName);
        assertThat(user.getEmail()).isEqualTo(email);
    }
}
