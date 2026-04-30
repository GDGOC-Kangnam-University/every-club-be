package gdgoc.everyclub.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // ── SignupSendOtpRequest ───────────────────────────────────────────

    @Test
    @DisplayName("SignupSendOtpRequest - 유효한 이메일 → 통과")
    void signupSendOtpRequest_ValidEmail() {
        Set<ConstraintViolation<SignupSendOtpRequest>> violations =
                validator.validate(new SignupSendOtpRequest("student@kangnam.ac.kr"));
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SignupSendOtpRequest - 이메일 형식 오류 → 실패")
    void signupSendOtpRequest_InvalidEmail() {
        Set<ConstraintViolation<SignupSendOtpRequest>> violations =
                validator.validate(new SignupSendOtpRequest("not-an-email"));
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("SignupSendOtpRequest - 빈 이메일 → 실패")
    void signupSendOtpRequest_BlankEmail() {
        Set<ConstraintViolation<SignupSendOtpRequest>> violations =
                validator.validate(new SignupSendOtpRequest(""));
        assertThat(violations).isNotEmpty();
    }

    // ── SignupVerifyOtpRequest ─────────────────────────────────────────

    @Test
    @DisplayName("SignupVerifyOtpRequest - 유효한 데이터 → 통과")
    void signupVerifyOtpRequest_Valid() {
        Set<ConstraintViolation<SignupVerifyOtpRequest>> violations =
                validator.validate(new SignupVerifyOtpRequest("student@kangnam.ac.kr", "ABC123"));
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SignupVerifyOtpRequest - 빈 코드 → 실패")
    void signupVerifyOtpRequest_BlankCode() {
        Set<ConstraintViolation<SignupVerifyOtpRequest>> violations =
                validator.validate(new SignupVerifyOtpRequest("student@kangnam.ac.kr", ""));
        assertThat(violations).isNotEmpty();
    }

    // ── LoginRequest ──────────────────────────────────────────────────

    @Test
    @DisplayName("LoginRequest - 유효한 데이터 → 통과")
    void loginRequest_Valid() {
        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(new LoginRequest("student@kangnam.ac.kr", "password123!"));
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("LoginRequest - 빈 비밀번호 → 실패")
    void loginRequest_BlankPassword() {
        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(new LoginRequest("student@kangnam.ac.kr", ""));
        assertThat(violations).isNotEmpty();
    }
}
