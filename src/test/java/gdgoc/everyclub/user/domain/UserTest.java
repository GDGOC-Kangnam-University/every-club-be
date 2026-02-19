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

    @Test
    @DisplayName("유저 생성 시 이름이 null이어도 객체는 생성된다 (DB 저장 시점에 실패)")
    void createUser_NullName() {
        // when
        User user = new User(null, "test@example.com");

        // then
        assertThat(user.getName()).isNull();
    }

    @Test
    @DisplayName("유저 정보 수정 시 이름을 null로 변경할 수 있다 (DB 저장 시점에 실패)")
    void update_NullName() {
        // given
        User user = new User("John Doe", "john@example.com");

        // when
        user.update(null);

        // then
        assertThat(user.getName()).isNull();
    }

    @Test
    @DisplayName("유저는 생성 시 빈 좋아요 동아리 목록을 가진다")
    void userHasEmptyLikedClubsByDefault() {
        // given
        User user = new User("John Doe", "john@example.com");

        // then
        assertThat(user.getLikedClubs()).isEmpty();
    }
}
