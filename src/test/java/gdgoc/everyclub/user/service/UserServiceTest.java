package gdgoc.everyclub.user.service;

import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.domain.UserRole;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    public static final String ERROR_CODE = "errorCode";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com");
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 생성 요청 시 올바른 데이터로 저장을 시도한다")
    void createUser() {
        // given
        UserCreateRequest request = new UserCreateRequest("John Doe", "john@example.com");

        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedUser, "id", 1L);
            return savedUser;
        });

        // when
        Long userId = userService.createUser(request);

        // then
        assertThat(userId).isEqualTo(1L);
        
        // 저장된 객체의 상태를 캡처하여 검증 (단순 호출 확인을 넘어섬)
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getName()).isEqualTo(request.name());
        assertThat(capturedUser.getEmail()).isEqualTo(request.email());
        // 기본 권한은 GUEST
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    @DisplayName("유저 생성 요청 시 request가 null이면 NullPointerException이 발생한다")
    void createUser_NullRequest() {
        // when & then
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 유저를 생성하려고 하면 예외가 발생한다")
    void createUser_DuplicateEmail() {
        // given
        String duplicateEmail = "duplicate@example.com";
        UserCreateRequest request = new UserCreateRequest("New User", duplicateEmail);

        // Mocking behavior: suppose the repository throws an exception for duplicate keys
        given(userRepository.save(any(User.class)))
                .willThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate email"));

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
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
        assertThat(users).containsExactly(user1, user2);
    }

    @Test
    @DisplayName("ID로 유저를 조회한다")
    void getUserById() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when
        User foundUser = userService.getUserById(userId);

        // then
        assertThat(foundUser).isEqualTo(testUser);
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
        UserUpdateRequest request = new UserUpdateRequest("John Smith");

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when
        userService.updateUser(userId, request);

        // then
        assertThat(testUser.getName()).isEqualTo("John Smith");
        // 이메일은 변경되지 않았음을 검증 (비즈니스 로직: 수정 시 이메일 불변)
        assertThat(testUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 유저 수정 시 예외가 발생한다")
    void updateUser_NotFound() {
        // given
        Long userId = 999L;
        UserUpdateRequest request = new UserUpdateRequest("John Smith");
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
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("유저 정보 수정 시 request가 null이면 NullPointerException이 발생한다")
    void updateUser_NullRequest() {
        // given
        Long userId = 1L;

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, null))
                .isInstanceOf(NullPointerException.class);
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
                .extracting(ERROR_CODE)
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }
}
