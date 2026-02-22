package gdgoc.everyclub.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("유저는 생성 시 기본적으로 GUEST 권한을 가진다")
    void userHasGuestRoleByDefault() {
        // given
        String email = "john@example.com";

        // when
        User user = User.builder()
                .email(email)
                .build();

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    @DisplayName("유저 정보 수정 시 닉네임은 변경되지만 이메일은 변경되지 않는다")
    void updateOnlyChangesNickname() {
        // given
        String originalNickname = "John Doe";
        String email = "john@example.com";
        User user = User.builder()
                .email(email)
                .nickname(originalNickname)
                .build();

        String newNickname = "John Smith";

        // when
        user.updateProfile(newNickname, null, null, null, null);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("유저 생성 시 닉네임이 null이어도 객체는 생성된다 (DB 저장 시점에 실패)")
    void createUser_NullNickname() {
        // when
        User user = User.builder()
                .email("test@example.com")
                .build();

        // then
        assertThat(user.getNickname()).isNull();
    }

    @Test
    @DisplayName("getName은 닉네임이 있으면 닉네임을, 없으면 이메일의 @ 앞부분을 반환한다")
    void getName_ReturnsNicknameOrEmailPrefix() {
        // given
        User userWithNickname = User.builder()
                .email("john@example.com")
                .nickname("Johnny")
                .build();

        User userWithoutNickname = User.builder()
                .email("jane@example.com")
                .build();

        // then
        assertThat(userWithNickname.getName()).isEqualTo("Johnny");
        assertThat(userWithoutNickname.getName()).isEqualTo("jane");
    }

    @Test
    @DisplayName("유저는 생성 시 모든 프로필 필드를 가질 수 있다")
    void createUser_WithAllProfileFields() {
        // given
        String email = "john@example.com";
        String nickname = "Johnny";
        String profileImageUrl = "https://example.com/image.jpg";
        String department = "Computer Science";
        String studentId = "20230001";
        String phoneNumber = "010-1234-5678";
        String bio = "Hello, I'm John!";

        // when
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .department(department)
                .studentId(studentId)
                .phoneNumber(phoneNumber)
                .bio(bio)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl);
        assertThat(user.getDepartment()).isEqualTo(department);
        assertThat(user.getStudentId()).isEqualTo(studentId);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getBio()).isEqualTo(bio);
    }

    @Test
    @DisplayName("유저는 생성 시 선택적 프로필 필드 없이도 생성될 수 있다")
    void createUser_WithOnlyRequiredFields() {
        // given
        String email = "john@example.com";

        // when
        User user = User.builder()
                .email(email)
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isNull();
        assertThat(user.getProfileImageUrl()).isNull();
        assertThat(user.getDepartment()).isNull();
        assertThat(user.getStudentId()).isNull();
        assertThat(user.getPhoneNumber()).isNull();
        assertThat(user.getBio()).isNull();
    }

    @Test
    @DisplayName("유저 프로필 정보를 업데이트할 수 있다")
    void updateProfile() {
        // given
        User user = User.builder()
                .email("john@example.com")
                .nickname("OldNickname")
                .build();

        // when
        user.updateProfile("NewNickname", "New Department", "20230002", "010-9999-8888", "New bio text");

        // then
        assertThat(user.getNickname()).isEqualTo("NewNickname");
        assertThat(user.getDepartment()).isEqualTo("New Department");
        assertThat(user.getStudentId()).isEqualTo("20230002");
        assertThat(user.getPhoneNumber()).isEqualTo("010-9999-8888");
        assertThat(user.getBio()).isEqualTo("New bio text");
    }

    @Test
    @DisplayName("유저 프로필 이미지를 업데이트할 수 있다")
    void updateProfileImage() {
        // given
        User user = User.builder()
                .email("john@example.com")
                .build();

        // when
        user.updateProfileImage("https://new-image.com/pic.jpg");

        // then
        assertThat(user.getProfileImageUrl()).isEqualTo("https://new-image.com/pic.jpg");
    }

    @Test
    @DisplayName("유저는 생성 시 updatedAt이 null이다 (최초 생성 시)")
    void createdUser_HasNullUpdatedAt() {
        // when
        User user = User.builder()
                .email("john@example.com")
                .build();

        // then
        assertThat(user.getUpdatedAt()).isNull();
    }
}
