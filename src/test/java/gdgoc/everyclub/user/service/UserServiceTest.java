package gdgoc.everyclub.user.service;

import gdgoc.everyclub.auth.service.SignupService;
import gdgoc.everyclub.common.exception.AuthErrorCode;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String ERROR_CODE = "errorCode";
    private static final String TEST_EMAIL = "john@kangnam.ac.kr";
    private static final String SIGNUP_TOKEN = "valid-signup-token";
    private static final String PASSWORD = "Password1!";

    private User testUser;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SignupService signupService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email(TEST_EMAIL)
                .nickname("Test User")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    @Test
    @DisplayName("유효한 signupToken으로 회원가입 시 User가 저장된다")
    void createUser() {
        // given
        UserCreateRequest request = new UserCreateRequest(SIGNUP_TOKEN, "John", null, PASSWORD, PASSWORD);

        given(signupService.consumeSignupToken(SIGNUP_TOKEN)).willReturn(TEST_EMAIL);
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(passwordEncoder.encode(PASSWORD)).willReturn("$2a$10$hashed");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        // when
        Long userId = userService.createUser(request);

        // then
        assertThat(userId).isEqualTo(1L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(saved.getNickname()).isEqualTo("John");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$hashed");
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    @DisplayName("비밀번호 불일치 시 PASSWORD_MISMATCH 예외 발생")
    void createUser_PasswordMismatch() {
        UserCreateRequest request = new UserCreateRequest(SIGNUP_TOKEN, "John", null, "Password1!", "Different1!");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(LogicException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(ValidationErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("유효하지 않은 signupToken → INVALID_SIGNUP_TOKEN 예외 발생")
    void createUser_InvalidSignupToken() {
        UserCreateRequest request = new UserCreateRequest("bad-token", "John", null, PASSWORD, PASSWORD);

        given(signupService.consumeSignupToken("bad-token"))
                .willThrow(new LogicException(AuthErrorCode.INVALID_SIGNUP_TOKEN));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(LogicException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(AuthErrorCode.INVALID_SIGNUP_TOKEN);
    }

    @Test
    @DisplayName("이미 가입된 이메일 → DUPLICATE_RESOURCE 예외 발생")
    void createUser_DuplicateEmail() {
        UserCreateRequest request = new UserCreateRequest(SIGNUP_TOKEN, "John", null, PASSWORD, PASSWORD);

        given(signupService.consumeSignupToken(SIGNUP_TOKEN)).willReturn(TEST_EMAIL);
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(LogicException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(BusinessErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("request가 null이면 NullPointerException 발생")
    void createUser_NullRequest() {
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("모든 유저를 조회한다")
    void getUsers() {
        User user1 = User.builder().email("user1@example.com").nickname("User1").build();
        User user2 = User.builder().email("user2@example.com").nickname("User2").build();
        given(userRepository.findAll()).willReturn(List.of(user1, user2));

        List<User> users = userService.getUsers();

        assertThat(users).hasSize(2).containsExactly(user1, user2);
    }

    @Test
    @DisplayName("ID로 유저를 조회한다")
    void getUserById() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        User found = userService.getUserById(1L);

        assertThat(found).isEqualTo(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 예외 발생")
    void getUserById_NotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(LogicException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("유저 정보를 수정한다")
    void updateUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        userService.updateUser(1L, new UserUpdateRequest("Updated", null, null, null, null));

        assertThat(testUser.getName()).isEqualTo("Updated");
        assertThat(testUser.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("존재하지 않는 유저 수정 시 예외 발생")
    void updateUser_NotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, new UserUpdateRequest("x", null, null, null, null)))
                .isInstanceOf(LogicException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("유저를 삭제한다")
    void deleteUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 유저 삭제 시 예외 발생")
    void deleteUser_NotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(LogicException.class)
                .extracting(ERROR_CODE)
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("수정 request가 null이면 NullPointerException 발생")
    void updateUser_NullRequest() {
        assertThatThrownBy(() -> userService.updateUser(1L, null))
                .isInstanceOf(NullPointerException.class);
    }
}
