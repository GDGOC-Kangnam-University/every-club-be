package gdgoc.everyclub.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.auth.dto.LoginRequest;
import gdgoc.everyclub.auth.dto.SignupSendOtpRequest;
import gdgoc.everyclub.auth.dto.SignupTokenResponse;
import gdgoc.everyclub.auth.dto.SignupVerifyOtpRequest;
import gdgoc.everyclub.auth.service.RateLimiterService;
import gdgoc.everyclub.auth.service.SignupService;
import gdgoc.everyclub.common.exception.AuthErrorCode;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.security.dto.TokenInfo;
import gdgoc.everyclub.security.jwt.JwtProvider;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

    @MockitoBean
    private SignupService signupService;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        doNothing().when(rateLimiterService).acquireOrThrow(anyString());
    }

    // ── signup/send-otp ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /signup/send-otp 성공")
    void signupSendOtp_Success() throws Exception {
        doNothing().when(signupService).sendOtp(anyString());

        mockMvc.perform(post("/api/v1/auth/signup/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupSendOtpRequest("student@kangnam.ac.kr"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /signup/send-otp - 이메일 형식 오류 → 400")
    void signupSendOtp_InvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupSendOtpRequest("not-an-email"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signup/send-otp - 학교 이메일 아님 → 400")
    void signupSendOtp_NotSchoolEmail() throws Exception {
        doThrow(new LogicException(ValidationErrorCode.INVALID_EMAIL_DOMAIN))
                .when(signupService).sendOtp(anyString());

        mockMvc.perform(post("/api/v1/auth/signup/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupSendOtpRequest("user@gmail.com"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signup/send-otp - 이미 가입된 이메일 → 409")
    void signupSendOtp_AlreadyRegistered() throws Exception {
        doThrow(new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE))
                .when(signupService).sendOtp(anyString());

        mockMvc.perform(post("/api/v1/auth/signup/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupSendOtpRequest("existing@kangnam.ac.kr"))))
                .andExpect(status().isConflict());
    }

    // ── signup/verify-otp ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /signup/verify-otp 성공 → signupToken 반환")
    void signupVerifyOtp_Success() throws Exception {
        when(signupService.verifyOtp(anyString(), anyString()))
                .thenReturn(new SignupTokenResponse("test-token", 600));

        mockMvc.perform(post("/api/v1/auth/signup/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupVerifyOtpRequest("student@kangnam.ac.kr", "ABC123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.signupToken").value("test-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(600));
    }

    @Test
    @DisplayName("POST /signup/verify-otp - OTP 불일치 → 401")
    void signupVerifyOtp_InvalidOtp() throws Exception {
        doThrow(new LogicException(AuthErrorCode.INVALID_OTP))
                .when(signupService).verifyOtp(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/signup/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupVerifyOtpRequest("student@kangnam.ac.kr", "WRONG"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /signup/verify-otp - code 누락 → 400")
    void signupVerifyOtp_BlankCode() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupVerifyOtpRequest("student@kangnam.ac.kr", ""))))
                .andExpect(status().isBadRequest());
    }

    // ── login ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /login 성공 → JWT 반환")
    void login_Success() throws Exception {
        User user = User.builder()
                .email("student@kangnam.ac.kr")
                .passwordHash("$2a$10$hashed")
                .nickname("테스터")
                .build();

        when(userRepository.findByEmail("student@kangnam.ac.kr")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123!", "$2a$10$hashed")).thenReturn(true);
        when(jwtProvider.generateToken(any())).thenReturn(
                TokenInfo.builder().grantType("Bearer").accessToken("jwt.token.here").expiresIn(3600L).build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("student@kangnam.ac.kr", "password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt.token.here"))
                .andExpect(jsonPath("$.data.grantType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /login - 존재하지 않는 이메일 → 401")
    void login_UserNotFound() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("nobody@kangnam.ac.kr", "password123!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /login - 비밀번호 불일치 → 401")
    void login_WrongPassword() throws Exception {
        User user = User.builder()
                .email("student@kangnam.ac.kr")
                .passwordHash("$2a$10$hashed")
                .nickname("테스터")
                .build();

        when(userRepository.findByEmail("student@kangnam.ac.kr")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("student@kangnam.ac.kr", "wrongpassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /login - 이메일 형식 오류 → 400")
    void login_InvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("not-an-email", "password123!"))))
                .andExpect(status().isBadRequest());
    }
}