package gdgoc.everyclub.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.repository.UserRepository;
import gdgoc.everyclub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails createUserDetails(Long userId) {
        return new CustomUserDetails(userId, "test@example.com", null, "ROLE_USER");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: UserService.createUser() creates user in database")
    void createUser_Integration() {
        // given
        UserCreateRequest request = new UserCreateRequest("newuser@example.com", "New User");

        // when
        Long userId = userService.createUser(request);

        // then
        assertThat(userId).isNotNull();
        User savedUser = userRepository.findById(userId).orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("New User");
        assertThat(savedUser.getRole().name()).isEqualTo("GUEST");
    }

    @Test
    @DisplayName("Integration: Creating user with existing email throws DataIntegrityViolationException")
    void createUser_DuplicateEmail_Integration() {
        // given - create first user
        UserCreateRequest request1 = new UserCreateRequest("duplicate@example.com", "User One");
        userService.createUser(request1);

        // Flush to ensure the first user is committed to database
        userRepository.flush();

        // when & then - try to create second user with same email
        UserCreateRequest request2 = new UserCreateRequest("duplicate@example.com", "User Two");
        assertThatThrownBy(() -> {
            userService.createUser(request2);
            userRepository.flush(); // Force the database constraint check
        })
                .isInstanceOf(gdgoc.everyclub.common.exception.LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("Integration: POST /api/users/signup creates a user without authentication")
    void signup_Integration() throws Exception {
        UserCreateRequest request = new UserCreateRequest("signup@example.com", "Signup User");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("signup@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("Signup User"));

        assertThat(userRepository.findByEmail("signup@example.com")).isPresent();
    }

    @Test
    @DisplayName("Integration: POST /api/users/signup returns 409 for duplicate email")
    void signup_DuplicateEmail_Integration() throws Exception {
        userService.createUser(new UserCreateRequest("duplicate-api@example.com", "User One"));

        UserCreateRequest duplicateRequest = new UserCreateRequest("duplicate-api@example.com", "User Two");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Integration: GET /api/users/me returns authenticated user's profile")
    void getMyProfile_Integration() throws Exception {
        // given - create and save a user
        UserCreateRequest createRequest = new UserCreateRequest("john@example.com", "John Doe");
        Long userId = userService.createUser(createRequest);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(user(createUserDetails(userId))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("John Doe"));
    }

    @Test
    @DisplayName("Integration: PATCH /api/users/me updates user's profile")
    void updateMyProfile_Integration() throws Exception {
        // given - create a user
        UserCreateRequest createRequest = new UserCreateRequest("jane@example.com", "Jane Doe");
        Long userId = userService.createUser(createRequest);

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "Jane Updated", "Computer Science", "20230001", "010-1234-5678", "Hello World"
        );

        // when & then
        mockMvc.perform(patch("/api/users/me")
                        .with(user(createUserDetails(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("Jane Updated"))
                .andExpect(jsonPath("$.data.department").value("Computer Science"))
                .andExpect(jsonPath("$.data.studentId").value("20230001"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.bio").value("Hello World"));

        // verify in database
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo("Jane Updated");
        assertThat(updatedUser.getDepartment()).isEqualTo("Computer Science");
    }

    @Test
    @DisplayName("Integration: PATCH /api/users/me supports partial updates")
    void updateMyProfile_Partial_Integration() throws Exception {
        // given - create a user
        UserCreateRequest createRequest = new UserCreateRequest("partial@example.com", "Original Name");
        Long userId = userService.createUser(createRequest);

        // update only nickname
        UserUpdateRequest updateRequest = new UserUpdateRequest("Partially Updated", null, null, null, null);

        // when & then
        mockMvc.perform(patch("/api/users/me")
                        .with(user(createUserDetails(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Partially Updated"));

        // verify other fields remain null
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo("Partially Updated");
        assertThat(updatedUser.getDepartment()).isNull();
    }

    @Test
    @DisplayName("Integration: DELETE /api/users/me deletes the user account")
    void deleteMyAccount_Integration() throws Exception {
        // given - create a user
        UserCreateRequest createRequest = new UserCreateRequest("delete@example.com", "To Be Deleted");
        Long userId = userService.createUser(createRequest);

        assertThat(userRepository.findById(userId)).isPresent();

        // when & then
        mockMvc.perform(delete("/api/users/me")
                        .with(user(createUserDetails(userId)))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // verify user is soft deleted - findById should return empty for soft-deleted records
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Integration: Full user lifecycle - create, read, update, delete")
    void fullUserLifecycle_Integration() throws Exception {
        // Step 1: Create user via service
        UserCreateRequest createRequest = new UserCreateRequest("lifecycle@example.com", "Lifecycle User");
        Long userId = userService.createUser(createRequest);
        assertThat(userId).isNotNull();

        // Step 2: Read user via GET /api/users/me
        mockMvc.perform(get("/api/users/me")
                        .with(user(createUserDetails(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("lifecycle@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("Lifecycle User"));

        // Step 3: Update user via PATCH /api/users/me
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "Updated Lifecycle User", "Engineering", "20231000", null, null
        );
        mockMvc.perform(patch("/api/users/me")
                        .with(user(createUserDetails(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Updated Lifecycle User"));

        // Step 4: Delete user via DELETE /api/users/me
        mockMvc.perform(delete("/api/users/me")
                        .with(user(createUserDetails(userId)))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify final state - user should be soft deleted (not findable by standard queries)
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Integration: GET /api/users/me returns 404 for non-existent user")
    void getMyProfile_NonExistentUser_Integration() throws Exception {
        // when & then - request with user ID that doesn't exist
        CustomUserDetails nonExistentUser = new CustomUserDetails(99999L, "nonexistent@example.com", null, "ROLE_USER");

        mockMvc.perform(get("/api/users/me")
                        .with(user(nonExistentUser)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }
}
