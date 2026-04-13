package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClubDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("ClubUpdateRequest 필수 필드 누락 시 유효성 검사에 실패한다")
    void clubUpdateRequest_ValidationFail() {
        ClubUpdateRequest request = ClubUpdateRequest.builder()
                .name("")
                .summary("")
                .recruitingStatus(null)
                .activityCycle("WEEKLY")
                .hasFee(false)
                .isPublic(true)
                .tags(List.of())
                .build();

        Set<ConstraintViolation<ClubUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}