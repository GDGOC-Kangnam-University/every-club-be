package gdgoc.everyclub.user.service;

import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저를 생성하고 ID를 반환한다")
    void createUser() {
        // given
        UserCreateRequest request = new UserCreateRequest("Name", "example@example.com");
        User user = new User(request);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", 1L);
            return savedUser;
        });

        // when
        Long userId = userService.createUser(request);

        // then
        assertThat(userId).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("모든 유저를 조회한다")
    void getUsers() {
        // given
        User user1 = new User("User1", "user1@example.com");
        User user2 = new User("User2", "user2@example.com");
        given(userRepository.findAll()).willReturn(List.of(user1, user2));

        // when
        List<User> users = userService.getUsers();

        // then
        assertThat(users).hasSize(2);
        assertThat(users).contains(user1, user2);
    }

    @Test
    @DisplayName("ID로 유저를 조회한다")
    void getUserById() {
        // given
        Long userId = 1L;
        User user = new User("Name", "example@example.com");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User foundUser = userService.getUserById(userId);

        // then
        assertThat(foundUser).isEqualTo(user);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 유저 조회 시 예외가 발생한다")
    void getUserById_NotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("유저 정보를 수정한다")
    void updateUser() {
        // given
        Long userId = 1L;
        User user = new User("Name", "example@example.com");
        UserUpdateRequest request = new UserUpdateRequest("Name2");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.updateUser(userId, request);

        // then
        assertThat(user.getName()).isEqualTo("Name2");
    }

    @Test
    @DisplayName("존재하지 않는 유저 수정 시 예외가 발생한다")
    void updateUser_NotFound() {
        // given
        Long userId = 999L;
        UserUpdateRequest request = new UserUpdateRequest("Name2");
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("유저를 삭제한다")
    void deleteUser() {
        // given
        Long userId = 1L;
        User user = new User("Name2", "example@example.com");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("존재하지 않는 유저 삭제 시 예외가 발생한다")
    void deleteUser_NotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }
}
