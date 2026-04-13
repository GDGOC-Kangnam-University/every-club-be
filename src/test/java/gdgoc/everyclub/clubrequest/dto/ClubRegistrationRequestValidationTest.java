package gdgoc.everyclub.clubrequest.dto;

import gdgoc.everyclub.club.domain.RecruitingStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class ClubRegistrationRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("필수값 전부 누락 시 expected constraint가 발생한다")
    void allRequiredFieldsMissing_violationsMatch() {
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("")
                .categoryId(null)
                .slug("")
                .summary("")
                .recruitingStatus(null)
                .hasFee(false)
                .isPublic(true)
                .build();

        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .hasSize(5)
                .extracting(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactlyInAnyOrder(
                        tuple("name", NotBlank.class),
                        tuple("categoryId", NotNull.class),
                        tuple("slug", NotBlank.class),
                        tuple("summary", NotBlank.class),
                        tuple("recruitingStatus", NotNull.class)
                );
    }

    @Test
    @DisplayName("정상 입력 시 violation이 없다")
    void validInput_noViolations() {
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("테스트동아리")
                .categoryId(1L)
                .slug("test-club")
                .summary("한 줄 소개입니다")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .build();

        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("동아리명 21자 초과 시 Size violation이 발생한다")
    void nameTooLong_violation() {
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("가".repeat(21))
                .categoryId(1L)
                .slug("test-club")
                .summary("한 줄 소개입니다")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .build();

        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        assertSingleViolation(violations, "name", Size.class);
    }

    @Test
    @DisplayName("slug 101자 초과 시 Size violation이 발생한다")
    void slugTooLong_violation() {
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("테스트동아리")
                .categoryId(1L)
                .slug("a".repeat(101))
                .summary("한 줄 소개입니다")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .build();

        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        assertSingleViolation(violations, "slug", Size.class);
    }

    private void assertSingleViolation(Set<ConstraintViolation<ClubRegistrationRequest>> violations,
                                       String property,
                                       Class<? extends Annotation> annotationType) {
        assertThat(violations)
                .hasSize(1)
                .extracting(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactly(tuple(property, annotationType));
    }
}
