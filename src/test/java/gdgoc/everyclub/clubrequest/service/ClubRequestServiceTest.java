package gdgoc.everyclub.clubrequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestPayload;
import gdgoc.everyclub.clubrequest.repository.ClubRequestRepository;
import gdgoc.everyclub.college.domain.College;
import gdgoc.everyclub.college.domain.Major;
import gdgoc.everyclub.college.repository.MajorRepository;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClubRequestServiceTest {

    @Mock
    private ClubRequestRepository clubRequestRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private UserService userService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ClubRequestService clubRequestService;

    private User requester;
    private Category departmentCategory;
    private Category centralCategory;
    private Category etcCategory;
    private College college;
    private Major major;

    @BeforeEach
    void setUp() {
        requester = new User("신청자", "requester@example.com");
        ReflectionTestUtils.setField(requester, "id", 1L);

        departmentCategory = new Category("학과동아리");
        ReflectionTestUtils.setField(departmentCategory, "id", 1L);

        centralCategory = new Category("중앙동아리");
        ReflectionTestUtils.setField(centralCategory, "id", 2L);

        etcCategory = new Category("기타");
        ReflectionTestUtils.setField(etcCategory, "id", 3L);

        college = new College("소프트웨어융합대학");
        ReflectionTestUtils.setField(college, "id", 1L);

        major = new Major("컴퓨터소프트웨어학부", college);
        ReflectionTestUtils.setField(major, "id", 1L);
    }

    // === 카테고리별 분기 테스트 (핵심 비즈니스 규칙) ===

    @Test
    @DisplayName("학과동아리 + majorId 있음 → 정상 저장")
    void createClubRequest_departmentClubWithMajor_success() {
        // given
        ClubRegistrationRequest request = createRequest(1L, 1L, "dept-club");
        stubCommonDependencies(departmentCategory, request.slug());
        given(majorRepository.findById(1L)).willReturn(Optional.of(major));
        stubSave();

        // when
        ClubRegistrationResponse response = clubRequestService.createClubRequest(1L, request);

        // then
        assertThat(response).isNotNull();
        verify(clubRequestRepository).save(any(ClubRequest.class));
    }

    @Test
    @DisplayName("학과동아리 + majorId 없음 → MAJOR_REQUIRED 에러")
    void createClubRequest_departmentClubWithoutMajor_throwsValidationError() {
        // given
        ClubRegistrationRequest request = createRequest(1L, null, "dept-club");
        given(clubRepository.existsBySlug("dept-club")).willReturn(false);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(departmentCategory));

        // when & then
        assertThatThrownBy(() -> clubRequestService.createClubRequest(1L, request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ValidationErrorCode.MAJOR_REQUIRED_FOR_DEPARTMENT_CLUB);
    }

    @Test
    @DisplayName("학과동아리 + 존재하지 않는 majorId → RESOURCE_NOT_FOUND")
    void createClubRequest_departmentClubWithInvalidMajor_throwsNotFound() {
        // given
        ClubRegistrationRequest request = createRequest(1L, 999L, "dept-club");
        given(clubRepository.existsBySlug("dept-club")).willReturn(false);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(departmentCategory));
        given(majorRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubRequestService.createClubRequest(1L, request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("중앙동아리 + majorId 없음 → 정상 저장")
    void createClubRequest_centralClubWithoutMajor_success() {
        // given
        ClubRegistrationRequest request = createRequest(2L, null, "central-club");
        stubCommonDependencies(centralCategory, request.slug());
        stubSave();

        // when
        ClubRegistrationResponse response = clubRequestService.createClubRequest(1L, request);

        // then
        assertThat(response).isNotNull();
        verify(clubRequestRepository).save(any(ClubRequest.class));
    }

    @Test
    @DisplayName("기타 + majorId 없음 → 정상 저장")
    void createClubRequest_etcClubWithoutMajor_success() {
        // given
        ClubRegistrationRequest request = createRequest(3L, null, "etc-club");
        stubCommonDependencies(etcCategory, request.slug());
        stubSave();

        // when
        ClubRegistrationResponse response = clubRequestService.createClubRequest(1L, request);

        // then
        assertThat(response).isNotNull();
        verify(clubRequestRepository).save(any(ClubRequest.class));
    }

    // === 유일성·존재 검증 ===

    @Test
    @DisplayName("중복 slug → DUPLICATE_RESOURCE")
    void createClubRequest_duplicateSlug_throwsDuplicateError() {
        // given
        ClubRegistrationRequest request = createRequest(1L, null, "existing-slug");
        given(clubRepository.existsBySlug("existing-slug")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> clubRequestService.createClubRequest(1L, request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("존재하지 않는 categoryId → RESOURCE_NOT_FOUND")
    void createClubRequest_invalidCategoryId_throwsNotFound() {
        // given
        ClubRegistrationRequest request = createRequest(999L, null, "new-club");
        given(clubRepository.existsBySlug("new-club")).willReturn(false);
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubRequestService.createClubRequest(1L, request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    // === 생성된 ClubRequest 상태 검증 ===

    @Test
    @DisplayName("저장된 ClubRequest의 status는 PENDING이다")
    void createClubRequest_savedEntityHasPendingStatus() {
        // given
        ClubRegistrationRequest request = createRequest(2L, null, "new-club");
        stubCommonDependencies(centralCategory, request.slug());
        stubSave();

        // when
        ClubRegistrationResponse response = clubRequestService.createClubRequest(1L, request);

        // then
        assertThat(response.status()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @DisplayName("저장된 ClubRequest의 publicId는 null이 아니다")
    void createClubRequest_savedEntityHasPublicId() {
        // given
        ClubRegistrationRequest request = createRequest(2L, null, "new-club");
        stubCommonDependencies(centralCategory, request.slug());
        stubSave();

        // when
        ClubRegistrationResponse response = clubRequestService.createClubRequest(1L, request);

        // then
        assertThat(response.publicId()).isNotNull();
    }

    @Test
    @DisplayName("payload JSON에 요청 필드가 올바르게 직렬화된다")
    void createClubRequest_payloadSerializedCorrectly() throws JsonProcessingException {
        // given
        ClubRegistrationRequest request = createRequest(2L, null, "payload-test");
        stubCommonDependencies(centralCategory, request.slug());

        ArgumentCaptor<ClubRequest> captor = ArgumentCaptor.forClass(ClubRequest.class);
        given(clubRequestRepository.save(captor.capture())).willAnswer(invocation -> {
            ClubRequest entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
            return entity;
        });

        // when
        clubRequestService.createClubRequest(1L, request);

        // then
        ClubRequest saved = captor.getValue();
        ClubRequestPayload payload = objectMapper.readValue(saved.getPayload(), ClubRequestPayload.class);
        assertThat(payload.name()).isEqualTo("테스트동아리");
        assertThat(payload.slug()).isEqualTo("payload-test");
        assertThat(payload.summary()).isEqualTo("한 줄 소개");
        assertThat(payload.recruitingStatus()).isEqualTo(RecruitingStatus.OPEN);
    }

    // === 헬퍼 메서드 ===

    private ClubRegistrationRequest createRequest(Long categoryId, Long majorId, String slug) {
        return ClubRegistrationRequest.builder()
                .name("테스트동아리")
                .categoryId(categoryId)
                .slug(slug)
                .summary("한 줄 소개")
                .description("상세 설명")
                .recruitingStatus(RecruitingStatus.OPEN)
                .majorId(majorId)
                .activityCycle("매주")
                .hasFee(false)
                .isPublic(true)
                .build();
    }

    private void stubCommonDependencies(Category category, String slug) {
        given(clubRepository.existsBySlug(slug)).willReturn(false);
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(userService.getUserById(1L)).willReturn(requester);
    }

    private void stubSave() {
        given(clubRequestRepository.save(any(ClubRequest.class))).willAnswer(invocation -> {
            ClubRequest entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "id", 1L);
            ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
            return entity;
        });
    }
}
