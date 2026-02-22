package gdgoc.everyclub.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.auth.EmailService;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails createUserDetails() {
        return new CustomUserDetails(1L, "john@example.com", null, "ROLE_USER");
    }

    @Test
    @DisplayName("GET /api/users/me - 현재 인증된 유저의 프로필을 반환한다")
    void getMyProfile() throws Exception {
        // given
        User user = User.builder()
                .email("john@example.com")
                .nickname("John Doe")
                .department("Computer Science")
                .studentId("20230001")
                .build();
        given(userService.getUserById(1L)).willReturn(user);

        // when & then
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
    @DisplayName("GET /api/users/me - 유저가 존재하지 않으면 404를 반환한다")
    void getMyProfile_NotFound() throws Exception {
        // given
        given(userService.getUserById(1L))
                .willThrow(new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(user(createUserDetails())))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("PATCH /api/users/me - 현재 인증된 유저의 프로필을 수정한다")
    void updateMyProfile() throws Exception {
        // given
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

        // when & then
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
    @DisplayName("PATCH /api/users/me - 부분 업데이트도 가능하다 (일부 필드만 전송)")
    void updateMyProfile_Partial() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("Updated Nickname", null, null, null, null);
        User updatedUser = User.builder()
                .email("john@example.com")
                .nickname("Updated Nickname")
                .build();
        given(userService.getUserById(1L)).willReturn(updatedUser);

        // when & then
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
    @DisplayName("DELETE /api/users/me - 현재 인증된 유저의 계정을 삭제한다 (204 No Content)")
    void deleteMyAccount() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/users/me")
                        .with(user(createUserDetails()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("POST /api/users/check-email - 학교 이메일인지 확인한다")
    void checkEmail() throws Exception {
        // given - EmailService expects @knu.ac.kr pattern
        given(emailService.isSchoolEmail("student@knu.ac.kr")).willReturn(true);
        given(emailService.isSchoolEmail("test@gmail.com")).willReturn(false);

        // when & then - school email
        mockMvc.perform(post("/api/users/check-email")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("student@knu.ac.kr"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(true));

        // when & then - non-school email
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
