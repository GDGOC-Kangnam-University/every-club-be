package gdgoc.everyclub.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.auth.domain.SignupVerification;
import gdgoc.everyclub.auth.repository.SignupVerificationRepository;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import gdgoc.everyclub.user.dto.UserUpdateRequest;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
    private UserRepository userRepository;

    @Autowired
    private SignupVerificationRepository signupVerificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        signupVerificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────

    /**
     * OTP 검증이 완료된 SignupVerification을 DB에 삽입하고 raw signupToken을 반환한다.
     * 실제 OTP 발송/검증 단계를 생략하는 테스트 전용 유틸.
     */
    private String prepareSignupToken(String email) throws Exception {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawToken);

        SignupVerification sv = SignupVerification.builder()
                .email(email)
                .otpCode("TESTCODE")
                .otpExpiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        sv.markVerified(tokenHash, LocalDateTime.now().plusMinutes(10));
        signupVerificationRepository.save(sv);

        return rawToken;
    }

    /** 테스트용 User를 직접 DB에 저장한다 (signup flow 생략). */
    private User saveUser(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Password1!"))
                .nickname(nickname)
                .build();
        user.markEmailAsVerified();
        return userRepository.save(user);
    }

    private CustomUserDetails userDetails(Long userId) {
        return new CustomUserDetails(userId, "test@kangnam.ac.kr", null, "GUEST");
    }

    private static String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // ── 회원가입 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users/signup - signupToken으로 회원가입 성공")
    void signup_Integration() throws Exception {
        String token = prepareSignupToken("newuser@kangnam.ac.kr");
        UserCreateRequest request = new UserCreateRequest(token, "신규유저", "20241234", "Password1!", "Password1!");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("newuser@kangnam.ac.kr"))
                .andExpect(jsonPath("$.data.nickname").value("신규유저"));

        assertThat(userRepository.findByEmail("newuser@kangnam.ac.kr")).isPresent();
    }

    @Test
    @DisplayName("POST /api/users/signup - signupToken 재사용 불가 (1회 사용)")
    void signup_TokenReuse_Rejected() throws Exception {
        String token = prepareSignupToken("once@kangnam.ac.kr");
        UserCreateRequest request = new UserCreateRequest(token, "유저", null, "Password1!", "Password1!");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 같은 토큰으로 재시도
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/users/signup - 비밀번호 불일치 → 400")
    void signup_PasswordMismatch() throws Exception {
        String token = prepareSignupToken("mismatch@kangnam.ac.kr");
        UserCreateRequest request = new UserCreateRequest(token, "유저", null, "Password1!", "Different1!");

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ── 프로필 조회/수정/삭제 ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users/me - 인증된 유저의 프로필 반환")
    void getMyProfile_Integration() throws Exception {
        User user = saveUser("john@kangnam.ac.kr", "John Doe");

        mockMvc.perform(get("/api/users/me")
                        .with(user(userDetails(user.getId()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("john@kangnam.ac.kr"))
                .andExpect(jsonPath("$.data.nickname").value("John Doe"));
    }

    @Test
    @DisplayName("PATCH /api/users/me - 프로필 수정 성공")
    void updateMyProfile_Integration() throws Exception {
        User user = saveUser("jane@kangnam.ac.kr", "Jane Doe");
        UserUpdateRequest update = new UserUpdateRequest("Jane Updated", "공학대학", "20230001", "010-1234-5678", "Hello");

        mockMvc.perform(patch("/api/users/me")
                        .with(user(userDetails(user.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Jane Updated"))
                .andExpect(jsonPath("$.data.studentId").value("20230001"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getNickname()).isEqualTo("Jane Updated");
        assertThat(updated.getDepartment()).isEqualTo("공학대학");
    }

    @Test
    @DisplayName("PATCH /api/users/me - 부분 수정 (나머지 필드 유지)")
    void updateMyProfile_Partial_Integration() throws Exception {
        User user = saveUser("partial@kangnam.ac.kr", "Original");

        mockMvc.perform(patch("/api/users/me")
                        .with(user(userDetails(user.getId())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest("Partially Updated", null, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Partially Updated"));

        assertThat(userRepository.findById(user.getId()).orElseThrow().getDepartment()).isNull();
    }

    @Test
    @DisplayName("DELETE /api/users/me - 소프트 삭제")
    void deleteMyAccount_Integration() throws Exception {
        User user = saveUser("delete@kangnam.ac.kr", "To Delete");

        mockMvc.perform(delete("/api/users/me")
                        .with(user(userDetails(user.getId())))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("GET /api/users/me - 존재하지 않는 유저 → 404")
    void getMyProfile_NonExistentUser_Integration() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .with(user(new CustomUserDetails(99999L, "none@kangnam.ac.kr", null, "GUEST"))))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("Integration: 회원가입 → 조회 → 수정 → 삭제 전체 라이프사이클")
    void fullUserLifecycle_Integration() throws Exception {
        // 1. 회원가입
        String token = prepareSignupToken("lifecycle@kangnam.ac.kr");
        UserCreateRequest createReq = new UserCreateRequest(token, "Lifecycle", null, "Password1!", "Password1!");

        String signupResult = mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        User createdUser = userRepository.findByEmail("lifecycle@kangnam.ac.kr").orElseThrow();
        Long userId = createdUser.getId();

        // 2. 프로필 조회
        mockMvc.perform(get("/api/users/me").with(user(userDetails(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Lifecycle"));

        // 3. 프로필 수정
        mockMvc.perform(patch("/api/users/me")
                        .with(user(userDetails(userId))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserUpdateRequest("Updated Lifecycle", null, null, null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Updated Lifecycle"));

        // 4. 삭제
        mockMvc.perform(delete("/api/users/me").with(user(userDetails(userId))).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(userId)).isEmpty();
    }
}
