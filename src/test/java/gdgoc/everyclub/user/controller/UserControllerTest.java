package gdgoc.everyclub.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.auth.EmailService;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유저 생성 요청 시 200 OK와 생성된 유저 ID를 반환한다")
    void createUser() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("John Doe", "john@example.com");
        given(userService.createUser(any(UserCreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @DisplayName("유저 생성 요청 시 필수 값이 누락되면 400 Bad Request를 반환한다")
    void createUser_InvalidInput() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("", ""); // Invalid

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("모든 유저 조회 시 200 OK와 유저 리스트를 반환한다")
    void getUsers() throws Exception {
        // given
        User user1 = new User("User1", "user1@example.com");
        User user2 = new User("User2", "user2@example.com");
        given(userService.getUsers()).willReturn(List.of(user1, user2));

        // when & then
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("User1"))
                .andExpect(jsonPath("$.data[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$.data[1].name").value("User2"));
    }

    @Test
    @DisplayName("특정 유저 조회 시 200 OK와 유저 정보를 반환한다")
    void getUser() throws Exception {
        // given
        Long userId = 1L;
        User user = new User("John Doe", "john@example.com");
        given(userService.getUserById(userId)).willReturn(user);

        // when & then
        mockMvc.perform(get("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 404 Not Found를 반환한다")
    void getUser_NotFound() throws Exception {
        // given
        Long userId = 999L;
        given(userService.getUserById(userId))
                .willThrow(new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(ResourceErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage()));
    }

    @Test
    @DisplayName("유저 정보 수정 시 200 OK를 반환한다")
    void updateUser() throws Exception {
        // given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("Updated Name");

        // when & then
        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(userService).updateUser(eq(userId), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("유저 삭제 시 200 OK를 반환한다")
    void deleteUser() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(delete("/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(userService).deleteUser(userId);
    }
}
