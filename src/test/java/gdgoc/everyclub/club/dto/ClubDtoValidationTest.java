package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("ClubCreateRequest 필수 필드 누락 시 유효성 검사에 실패한다")
    void clubCreateRequest_ValidationFail() {
        // given
        ClubCreateRequest request = new ClubCreateRequest(
                "", // name empty
                null, // authorId null
                null, // categoryId null
                "", // slug empty
                "", // summary empty
                null, // description ok
                null, // logoUrl ok
                null, // bannerUrl ok
                null, // joinFormUrl ok
                null, // recruitingStatus null
                null, // department ok
                null, // activityCycle ok
                false, // hasFee
                true // isPublic
        );

        // when
        Set<ConstraintViolation<ClubCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(6);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Name must not be empty",
                        "Author ID must not be null",
                        "Category ID must not be null",
                        "Slug must not be empty",
                        "Summary must not be empty",
                        "Recruiting status must not be null"
                );
    }
}
