package gdgoc.everyclub.clubrequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import gdgoc.everyclub.clubrequest.domain.RequestStatus;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationRequest;
import gdgoc.everyclub.clubrequest.dto.ClubRegistrationResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestAdminResponse;
import gdgoc.everyclub.clubrequest.dto.ClubRequestPayload;
import gdgoc.everyclub.clubrequest.dto.ClubRequestRejectRequest;
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
import java.util.List;
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

    private static final UUID PUBLIC_ID = UUID.randomUUID();
    private static final Long ADMIN_ID = 99L;

    private User requester;
    private User admin;
    private Category departmentCategory;
    private Category centralCategory;
    private Category etcCategory;
    private College college;
    private Major major;

    @BeforeEach
    void setUp() {
        requester = new User("신청자", "requester@example.com");
        ReflectionTestUtils.setField(requester, "id", 1L);

        admin = new User("관리자", "admin@example.com");
        ReflectionTestUtils.setField(admin, "id", ADMIN_ID);

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

    // =====================================================================
    // createClubRequest — 카테고리별 분기 테스트 (핵심 비즈니스 규칙)
    // =====================================================================

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

    // =====================================================================
    // createClubRequest — 유일성·존재 검증
    // =====================================================================

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

    // =====================================================================
    // createClubRequest — 생성된 ClubRequest 상태 검증
    // =====================================================================

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

    // =====================================================================
    // approveClubRequest — 승인 워크플로우
    // =====================================================================

    @Test
    @DisplayName("승인 시 ClubRequest의 status가 APPROVED로 변경된다")
    void approveClubRequest_changesRequestStatusToApproved() {
        // given
        ClubRequest pending = buildPendingRequest("approve-club", 2L);
        stubApproveHappyPath(pending, "approve-club", centralCategory);

        // when
        clubRequestService.approveClubRequest(PUBLIC_ID, ADMIN_ID);

        // then
        assertThat(pending.getStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    @DisplayName("승인 시 생성되는 Club의 author는 신청자(admin이 아님)다")
    void approveClubRequest_createdClubHasRequesterAsAuthor() {
        // given
        ClubRequest pending = buildPendingRequest("author-club", 2L);
        stubApproveHappyPath(pending, "author-club", centralCategory);

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        given(clubRepository.save(clubCaptor.capture())).willAnswer(inv -> inv.getArgument(0));

        // when
        clubRequestService.approveClubRequest(PUBLIC_ID, ADMIN_ID);

        // then
        Club created = clubCaptor.getValue();
        assertThat(created.getAuthor()).isEqualTo(requester);
        assertThat(created.getAuthor()).isNotEqualTo(admin);
    }

    @Test
    @DisplayName("승인 시 생성되는 Club의 slug는 payload에 저장된 slug와 동일하다")
    void approveClubRequest_createdClubHasPayloadSlug() {
        // given
        ClubRequest pending = buildPendingRequest("slug-from-payload", 2L);
        stubApproveHappyPath(pending, "slug-from-payload", centralCategory);

        ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        given(clubRepository.save(clubCaptor.capture())).willAnswer(inv -> inv.getArgument(0));

        // when
        clubRequestService.approveClubRequest(PUBLIC_ID, ADMIN_ID);

        // then
        assertThat(clubCaptor.getValue().getSlug()).isEqualTo("slug-from-payload");
    }

    @Test
    @DisplayName("이미 승인된 요청을 재승인 시도하면 ALREADY_PROCESSED 에러")
    void approveClubRequest_alreadyApproved_throwsAlreadyProcessed() {
        // given
        ClubRequest alreadyApproved = buildPendingRequest("some-club", 2L);
        alreadyApproved.approve(admin); // 이미 승인 처리됨
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(alreadyApproved));

        // when & then
        assertThatThrownBy(() -> clubRequestService.approveClubRequest(PUBLIC_ID, ADMIN_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("이미 거절된 요청을 승인 시도하면 ALREADY_PROCESSED 에러")
    void approveClubRequest_alreadyRejected_throwsAlreadyProcessed() {
        // given
        ClubRequest alreadyRejected = buildPendingRequest("some-club", 2L);
        alreadyRejected.reject(admin, "거절 사유");
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(alreadyRejected));

        // when & then
        assertThatThrownBy(() -> clubRequestService.approveClubRequest(PUBLIC_ID, ADMIN_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("존재하지 않는 publicId 승인 시도 → RESOURCE_NOT_FOUND")
    void approveClubRequest_notFound_throwsNotFound() {
        // given
        given(clubRequestRepository.findByPublicId(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubRequestService.approveClubRequest(UUID.randomUUID(), ADMIN_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("승인 시점에 slug가 이미 사용 중이면 DUPLICATE_RESOURCE 에러")
    void approveClubRequest_slugTakenAtApprovalTime_throwsDuplicateResource() {
        // given: 신청 이후 동일 slug의 동아리가 생성된 상황
        ClubRequest pending = buildPendingRequest("taken-slug", 2L);
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(pending));
        given(clubRepository.existsBySlug("taken-slug")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> clubRequestService.approveClubRequest(PUBLIC_ID, ADMIN_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.DUPLICATE_RESOURCE);
    }

    // =====================================================================
    // rejectClubRequest — 거절 워크플로우
    // =====================================================================

    @Test
    @DisplayName("거절 시 ClubRequest의 status가 REJECTED로 변경된다")
    void rejectClubRequest_changesStatusToRejected() {
        // given
        ClubRequest pending = buildPendingRequest("reject-club", 2L);
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(pending));
        given(userService.getUserById(ADMIN_ID)).willReturn(admin);

        // when
        clubRequestService.rejectClubRequest(PUBLIC_ID, ADMIN_ID, new ClubRequestRejectRequest("규정 위반"));

        // then
        assertThat(pending.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }

    @Test
    @DisplayName("거절 시 전달한 adminMemo가 ClubRequest에 저장된다")
    void rejectClubRequest_savesAdminMemoInRequest() {
        // given
        ClubRequest pending = buildPendingRequest("memo-club", 2L);
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(pending));
        given(userService.getUserById(ADMIN_ID)).willReturn(admin);
        String memo = "신청 내용이 기준에 미달합니다.";

        // when
        clubRequestService.rejectClubRequest(PUBLIC_ID, ADMIN_ID, new ClubRequestRejectRequest(memo));

        // then
        assertThat(pending.getAdminMemo()).isEqualTo(memo);
    }

    @Test
    @DisplayName("이미 처리된 요청을 거절 시도하면 ALREADY_PROCESSED 에러")
    void rejectClubRequest_alreadyProcessed_throwsAlreadyProcessed() {
        // given
        ClubRequest alreadyApproved = buildPendingRequest("some-club", 2L);
        alreadyApproved.approve(admin);
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(alreadyApproved));

        // when & then
        assertThatThrownBy(() -> clubRequestService.rejectClubRequest(
                PUBLIC_ID, ADMIN_ID, new ClubRequestRejectRequest("사유")))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("존재하지 않는 publicId 거절 시도 → RESOURCE_NOT_FOUND")
    void rejectClubRequest_notFound_throwsNotFound() {
        // given
        given(clubRequestRepository.findByPublicId(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubRequestService.rejectClubRequest(
                UUID.randomUUID(), ADMIN_ID, new ClubRequestRejectRequest("사유")))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    // =====================================================================
    // getClubRequest — 단건 조회
    // =====================================================================

    @Test
    @DisplayName("존재하는 publicId로 조회하면 신청자 정보와 status가 응답에 포함된다")
    void getClubRequest_returnsRequesterInfoAndStatus() {
        // given
        ClubRequest pending = buildPendingRequest("query-club", 2L);
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(pending));

        // when
        ClubRequestAdminResponse response = clubRequestService.getClubRequest(PUBLIC_ID);

        // then
        assertThat(response.requesterName()).isEqualTo(requester.getName());
        assertThat(response.requesterEmail()).isEqualTo(requester.getEmail());
        assertThat(response.status()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @DisplayName("존재하는 publicId로 조회하면 payload의 slug가 응답에 포함된다")
    void getClubRequest_returnsPayloadSlug() {
        // given
        ClubRequest pending = buildPendingRequest("check-slug", 2L);
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(pending));

        // when
        ClubRequestAdminResponse response = clubRequestService.getClubRequest(PUBLIC_ID);

        // then
        assertThat(response.payload().slug()).isEqualTo("check-slug");
    }

    @Test
    @DisplayName("존재하지 않는 publicId로 조회 → RESOURCE_NOT_FOUND")
    void getClubRequest_notFound_throwsNotFound() {
        // given
        given(clubRequestRepository.findByPublicId(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubRequestService.getClubRequest(UUID.randomUUID()))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    // =====================================================================
    // getClubRequests — 목록 조회
    // =====================================================================

    @Test
    @DisplayName("신청 목록 조회 시 모든 신청 항목이 반환된다")
    void getClubRequests_returnsAllRequests() {
        // given
        ClubRequest first = buildPendingRequest("club-a", 2L);
        ClubRequest second = buildPendingRequest("club-b", 2L);
        given(clubRequestRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(first, second));

        // when
        List<ClubRequestAdminResponse> result = clubRequestService.getClubRequests();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClubRequestAdminResponse::requesterName)
                .containsOnly(requester.getName());
    }

    @Test
    @DisplayName("신청이 없을 때 빈 목록을 반환한다")
    void getClubRequests_returnsEmptyListWhenNoRequests() {
        // given
        given(clubRequestRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of());

        // when
        List<ClubRequestAdminResponse> result = clubRequestService.getClubRequests();

        // then
        assertThat(result).isEmpty();
    }

    // =====================================================================
    // 헬퍼 메서드
    // =====================================================================

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

    /**
     * 테스트용 PENDING 상태 ClubRequest 생성.
     * payload에는 slug와 categoryId만 의미 있게 설정하고 나머지는 기본값.
     */
    private ClubRequest buildPendingRequest(String slug, Long categoryId) {
        String payloadJson = buildPayloadJson(slug, categoryId);
        ClubRequest request = ClubRequest.builder()
                .requestedBy(requester)
                .payload(payloadJson)
                .build();
        ReflectionTestUtils.setField(request, "publicId", PUBLIC_ID);
        return request;
    }

    private String buildPayloadJson(String slug, Long categoryId) {
        ClubRequestPayload payload = ClubRequestPayload.builder()
                .name("테스트동아리")
                .categoryId(categoryId)
                .slug(slug)
                .summary("한 줄 소개")
                .description("상세 설명")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("매주")
                .hasFee(false)
                .isPublic(false)
                .build();
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * approveClubRequest 정상 케이스 stub 설정 공통 헬퍼.
     * clubRepository.save()는 각 테스트에서 필요에 따라 별도로 stub한다.
     * (ArgumentCaptor 사용 테스트와의 충돌 방지)
     */
    private void stubApproveHappyPath(ClubRequest pending, String slug, Category category) {
        given(clubRequestRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(pending));
        given(clubRepository.existsBySlug(slug)).willReturn(false);
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(userService.getUserById(ADMIN_ID)).willReturn(admin);
    }
}