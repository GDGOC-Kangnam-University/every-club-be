package gdgoc.everyclub.clubrequest.dto;

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

class ClubRegistrationRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("필수값 전부 누락 시 violation 개수가 일치한다")
    void allRequiredFieldsMissing_violationsMatch() {
        // given: name, slug, summary = blank / categoryId, recruitingStatus = null
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("")
                .categoryId(null)
                .slug("")
                .summary("")
                .recruitingStatus(null)
                .hasFee(false)
                .isPublic(true)
                .build();

        // when
        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        // then — @NotBlank(name, slug, summary) + @NotNull(categoryId, recruitingStatus) = 5개
        assertThat(violations).hasSize(5);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "동아리명은 필수입니다",
                        "카테고리 ID는 필수입니다",
                        "슬러그는 필수입니다",
                        "한 줄 소개는 필수입니다",
                        "모집 상태는 필수입니다"
                );
    }

    @Test
    @DisplayName("정상 입력 → violation 0개")
    void validInput_noViolations() {
        // given
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("테스트동아리")
                .categoryId(1L)
                .slug("test-club")
                .summary("한 줄 소개입니다")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .build();

        // when
        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("동아리명 21자 초과 → violation")
    void nameTooLong_violation() {
        // given
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("가".repeat(21))
                .categoryId(1L)
                .slug("test-club")
                .summary("한 줄 소개입니다")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .build();

        // when
        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("동아리명은 20자 이하여야 합니다");
    }

    @Test
    @DisplayName("slug 101자 초과 → violation")
    void slugTooLong_violation() {
        // given
        ClubRegistrationRequest request = ClubRegistrationRequest.builder()
                .name("테스트동아리")
                .categoryId(1L)
                .slug("a".repeat(101))
                .summary("한 줄 소개입니다")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .build();

        // when
        Set<ConstraintViolation<ClubRegistrationRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("슬러그는 100자 이하여야 합니다");
    }
}
