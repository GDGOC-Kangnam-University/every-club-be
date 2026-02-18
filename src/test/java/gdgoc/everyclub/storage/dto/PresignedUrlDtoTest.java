package gdgoc.everyclub.storage.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PresignedUrlDtoTest {

    @Autowired
    private Validator validator;

    @Test
    void presignedUrlRequestValidationShouldFailForEmptyFields() {
        PresignedUrlRequest request = PresignedUrlRequest.builder()
                .fileName("")
                .contentType("")
                .build();

        Set<ConstraintViolation<PresignedUrlRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void presignedUrlRequestValidationShouldPassForValidFields() {
        PresignedUrlRequest request = PresignedUrlRequest.builder()
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .build();

        Set<ConstraintViolation<PresignedUrlRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
