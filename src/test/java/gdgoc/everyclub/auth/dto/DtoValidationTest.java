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

    @Test
    @DisplayName("SendOtpRequest validation passes with valid email")
    void sendOtpRequest_ValidEmail_Passes() {
        // given
        SendOtpRequest request = new SendOtpRequest();
        request.setEmail("test@example.com");

        // when
        Set<ConstraintViolation<SendOtpRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SendOtpRequest validation fails with invalid email")
    void sendOtpRequest_InvalidEmail_Fails() {
        // given
        SendOtpRequest request = new SendOtpRequest();
        request.setEmail("invalid-email");

        // when
        Set<ConstraintViolation<SendOtpRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Invalid email format");
    }

    @Test
    @DisplayName("SendOtpRequest validation fails with blank email")
    void sendOtpRequest_BlankEmail_Fails() {
        // given
        SendOtpRequest request = new SendOtpRequest();
        request.setEmail("");

        // when
        Set<ConstraintViolation<SendOtpRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("VerifyOtpRequest validation passes with valid data")
    void verifyOtpRequest_ValidData_Passes() {
        // given
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setCode("ABC123");

        // when
        Set<ConstraintViolation<VerifyOtpRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("VerifyOtpRequest validation fails with blank code")
    void verifyOtpRequest_BlankCode_Fails() {
        // given
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setCode("");

        // when
        Set<ConstraintViolation<VerifyOtpRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
    }
}