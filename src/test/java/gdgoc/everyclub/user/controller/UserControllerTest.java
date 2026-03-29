package gdgoc.everyclub.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.auth.EmailService;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.security.jwt.JwtProvider;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails createUserDetails() {
        return new CustomUserDetails(1L, "john@example.com", null, "ROLE_USER");
    }

    @Test
    @DisplayName("POST /api/users/signup returns created user")
    void signup() throws Exception {
        UserCreateRequest request = new UserCreateRequest("valid-token", "John Doe", null, "Password1!", "Password1!");
        User createdUser = User.builder()
                .email("john@kangnam.ac.kr")
                .nickname("John Doe")
                .build();

        given(userService.createUser(any(UserCreateRequest.class))).willReturn(1L);
        given(userService.getUserById(1L)).willReturn(createdUser);

        mockMvc.perform(post("/api/users/signup")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("john@kangnam.ac.kr"))
                .andExpect(jsonPath("$.data.nickname").value("John Doe"));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/users/signup returns 409 for duplicate email")
    void signup_DuplicateEmail() throws Exception {
        UserCreateRequest request = new UserCreateRequest("valid-token", "John Doe", null, "Password1!", "Password1!");
        given(userService.createUser(any(UserCreateRequest.class)))
                .willThrow(new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE));

        mockMvc.perform(post("/api/users/signup")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("GET /api/users/me returns the authenticated user's profile")
    void getMyProfile() throws Exception {
        User user = User.builder()
                .email("john@example.com")
                .nickname("John Doe")
                .department("Computer Science")
                .studentId("20230001")
                .build();
        given(userService.getUserById(1L)).willReturn(user);

        mockMvc.perform(get("/api/users/me")
                        .with(user(createUserDetails())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.department").value("Computer Science"))
                .andExpect(jsonPath("$.data.studentId").value("20230001"));
    }

    @Test
    @DisplayName("GET /api/users/me returns 404 when the user does not exist")
    void getMyProfile_NotFound() throws Exception {
        given(userService.getUserById(1L))
                .willThrow(new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        mockMvc.perform(get("/api/users/me")
                        .with(user(createUserDetails())))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("PATCH /api/users/me updates the current user's profile")
    void updateMyProfile() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Updated Nickname", "Computer Science", "20230001", "010-1234-5678", "Hello!");
        User updatedUser = User.builder()
                .email("john@example.com")
                .nickname("Updated Nickname")
                .department("Computer Science")
                .studentId("20230001")
                .phoneNumber("010-1234-5678")
                .bio("Hello!")
                .build();
        given(userService.getUserById(1L)).willReturn(updatedUser);

        mockMvc.perform(patch("/api/users/me")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("Updated Nickname"))
                .andExpect(jsonPath("$.data.department").value("Computer Science"));

        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/users/me supports partial updates")
    void updateMyProfile_Partial() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Updated Nickname", null, null, null, null);
        User updatedUser = User.builder()
                .email("john@example.com")
                .nickname("Updated Nickname")
                .build();
        given(userService.getUserById(1L)).willReturn(updatedUser);

        mockMvc.perform(patch("/api/users/me")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/users/me deletes the current user's account")
    void deleteMyAccount() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .with(user(createUserDetails()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("POST /api/users/check-email returns whether the email is a school email")
    void checkEmail() throws Exception {
        given(emailService.isSchoolEmail("student@kangnam.ac.kr")).willReturn(true);
        given(emailService.isSchoolEmail("test@gmail.com")).willReturn(false);

        mockMvc.perform(post("/api/users/check-email")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("student@kangnam.ac.kr"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/api/users/check-email")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("test@gmail.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }
}
