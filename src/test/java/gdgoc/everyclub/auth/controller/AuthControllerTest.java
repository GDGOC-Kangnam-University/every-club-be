package gdgoc.everyclub.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.auth.dto.SendOtpRequest;
import gdgoc.everyclub.auth.dto.VerifyOtpRequest;
import gdgoc.everyclub.auth.service.OtpService;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OtpService otpService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .nickname("Test User")
                .build();
    }

    @Test
    @DisplayName("POST /send-otp returns success when user exists")
    void sendOtp_UserExists_ReturnsSuccess() throws Exception {
        // given
        SendOtpRequest request = new SendOtpRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp()).thenReturn("ABC123");
        doNothing().when(otpService).saveVerification(any(), anyString());
        doNothing().when(otpService).sendOtpEmail(anyString(), anyString());

        // when/then
        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    @DisplayName("POST /send-otp returns error when user not found")
    void sendOtp_UserNotFound_ReturnsError() throws Exception {
        // given
        SendOtpRequest request = new SendOtpRequest();
        request.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when/then
        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.success").value(false));
    }

    @Test
    @DisplayName("POST /send-otp returns error with invalid email format")
    void sendOtp_InvalidEmail_ReturnsError() throws Exception {
        // given
        SendOtpRequest request = new SendOtpRequest();
        request.setEmail("invalid-email");

        // when/then
        mockMvc.perform(post("/api/v1/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /verify returns success with valid code")
    void verify_ValidCode_ReturnsSuccess() throws Exception {
        // given
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setCode("ABC123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.verifyOtp(testUser.getId(), "ABC123")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when/then
        mockMvc.perform(post("/api/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    @DisplayName("POST /verify returns error with invalid code")
    void verify_InvalidCode_ReturnsError() throws Exception {
        // given
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setCode("WRONG");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.verifyOtp(testUser.getId(), "WRONG")).thenReturn(false);

        // when/then
        mockMvc.perform(post("/api/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.success").value(false));
    }

    @Test
    @DisplayName("POST /verify returns error when user not found")
    void verify_UserNotFound_ReturnsError() throws Exception {
        // given
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("nonexistent@example.com");
        request.setCode("ABC123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when/then
        mockMvc.perform(post("/api/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.success").value(false));
    }

    @Test
    @DisplayName("POST /verify returns error with blank code")
    void verify_BlankCode_ReturnsError() throws Exception {
        // given
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setCode("");

        // when/then
        mockMvc.perform(post("/api/v1/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}