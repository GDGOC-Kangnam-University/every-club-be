package gdgoc.everyclub.user.domain;

import gdgoc.everyclub.user.dto.UserCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("이름과 이메일로 유저를 생성할 수 있다")
    void createUser() {
        // given
        String name = "Name";
        String email = "example@example.com";

        // when
        User user = new User(name, email);

        // then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    @DisplayName("UserCreateRequest로 유저를 생성할 수 있다")
    void createUserFromRequest() {
        // given
        UserCreateRequest request = new UserCreateRequest("Name", "example@example.com");

        // when
        User user = new User(request);

        // then
        assertThat(user.getName()).isEqualTo(request.name());
        assertThat(user.getEmail()).isEqualTo(request.email());
    }

    @Test
    @DisplayName("유저 이름을 수정할 수 있다")
    void updateUser() {
        // given
        User user = new User("Name", "example@example.com");
        String newName = "Name2";

        // when
        user.update(newName);

        // then
        assertThat(user.getName()).isEqualTo(newName);
    }
}
