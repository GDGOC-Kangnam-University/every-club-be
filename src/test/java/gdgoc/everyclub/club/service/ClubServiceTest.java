package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.domain.Tag;
import gdgoc.everyclub.club.dto.*;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.club.repository.TagRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MajorRepository majorRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ClubService clubService;

    private User author;
    private Category category;
    private Club club;

    @BeforeEach
    void setUp() {
        author = User.builder().email("author@example.com").nickname("Author").build();
        ReflectionTestUtils.setField(author, "id", 1L);

        category = new Category("Academic");
        ReflectionTestUtils.setField(category, "id", 1L);

        club = Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);
    }

    @Test
    @DisplayName("동아리를 생성한다")
    void createClub() {
        // given
        ClubCreateRequest request = createCreateRequest("new-slug");
        given(userService.getUserById(1L)).willReturn(author);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(clubRepository.existsBySlug("new-slug")).willReturn(false);
        given(clubRepository.save(any(Club.class))).willAnswer(invocation -> {
            Club savedClub = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedClub, "id", 1L);
            return savedClub;
        });
        given(tagRepository.findByName(any())).willReturn(java.util.Optional.empty());
        given(tagRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Long clubId = clubService.createClub(request);

        // then
        assertThat(clubId).isEqualTo(1L);
        verify(clubRepository).save(any(Club.class));
    }

    @Test
    @DisplayName("slug가 중복되면 동아리 생성 시 예외가 발생한다")
    void createClub_DuplicateSlug() {
        // given
        ClubCreateRequest request = createCreateRequest("duplicate-slug");
        given(clubRepository.existsBySlug("duplicate-slug")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> clubService.createClub(request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("동아리 생성 시 존재하지 않는 작성자 ID면 예외가 발생한다")
    void createClub_AuthorNotFound() {
        // given
        ClubCreateRequest request = createCreateRequest("new-slug");
        given(userService.getUserById(1L))
                .willThrow(new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> clubService.createClub(request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("동아리 생성 시 존재하지 않는 카테고리 ID면 예외가 발생한다")
    void createClub_CategoryNotFound() {
        // given
        ClubCreateRequest request = createCreateRequest("new-slug");
        given(userService.getUserById(1L)).willReturn(author);
        given(clubRepository.existsBySlug("new-slug")).willReturn(false);
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubService.createClub(request))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("동아리 생성 시 request가 null이면 NullPointerException이 발생한다")
    void createClub_NullRequest() {
        // when & then
        assertThatThrownBy(() -> clubService.createClub(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("공개된 동아리만 전체 조회한다")
    void getClubs() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findAllByIsPublicTrue(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(club)));

        // when
        Page<Club> clubs = clubService.getClubs(pageRequest);

        // then
        assertThat(clubs.getContent()).hasSize(1);
        assertThat(clubs.getContent().get(0).getName()).isEqualTo("Name");
    }

    @Test
    @DisplayName("동아리 전체 조회 시 Pageable이 null이면 예외가 발생한다")
    void getClubs_NullPageable() {
        // when & then
        assertThatThrownBy(() -> clubService.getClubs(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ID로 동아리를 조회한다")
    void getClubById() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when
        Club foundClub = clubService.getClubById(1L);

        // then
        assertThat(foundClub).isEqualTo(club);
    }

    @Test
    @DisplayName("공개된 동아리를 ID로 조회한다")
    void getPublicClubById() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when
        ClubDetailResponse response = clubService.getPublicClubById(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Name");
    }

    @Test
    @DisplayName("비공개 동아리 조회 시 예외가 발생한다")
    void getPublicClubById_PrivateClub() {
        // given
        ReflectionTestUtils.setField(club, "isPublic", false);
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when & then
        assertThatThrownBy(() -> clubService.getPublicClubById(1L))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 동아리 조회 시 예외가 발생한다")
    void getClubById_NotFound() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubService.getClubById(1L))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("동아리를 수정한다")
    void updateClub() {
        // given
        ClubUpdateRequest request = createUpdateRequest();
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));
        given(tagRepository.findByName(any())).willReturn(java.util.Optional.empty());
        given(tagRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        clubService.updateClub(1L, request);

        // then
        assertThat(club.getName()).isEqualTo("New Name");
        assertThat(club.getSummary()).isEqualTo("New Summary");
    }

    @Test
    @DisplayName("정규화 결과가 같은 태그들은 한 번만 매핑한다")
    void updateClub_DeduplicatesNormalizedTags() {
        // given
        Tag savedTag = Tag.of("coding");
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));
        given(tagRepository.findByName("coding")).willReturn(Optional.empty());
        given(tagRepository.save(any())).willReturn(savedTag);

        // when
        clubService.updateClub(1L, ClubUpdateRequest.builder()
                .name("New Name")
                .summary("New Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .tags(List.of(" coding ", "#coding", "CODING"))
                .build());

        // then
        assertThat(club.getTagNames()).containsExactly("coding");
        verify(tagRepository).findByName("coding");
        verify(tagRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("동아리 수정 시 request가 null이면 NullPointerException이 발생한다")
    void updateClub_NullRequest() {
        // given
        Long clubId = 1L;

        // when & then
        assertThatThrownBy(() -> clubService.updateClub(clubId, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("태그로 동아리를 검색한다 (Tag/ClubTag 조인 기반 정확 매칭)")
    void searchClubsByTag() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of(club)));
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));
        given(clubRepository.findLikeCountsByIds(List.of(1L))).willReturn(List.of());

        // when
        Page<ClubSummaryResponse> result = clubService.searchClubsByTag("운동", pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(clubRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("동아리를 삭제한다")
    void deleteClub() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when
        clubService.deleteClub(1L);

        // then
        verify(clubRepository).delete(club);
    }

    @Test
    @DisplayName("태그 검색 시 tag가 null이면 예외가 발생한다")
    void searchClubsByTag_NullTag() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> clubService.searchClubsByTag(null, pageRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag cannot be null or blank");
    }

    @Test
    @DisplayName("태그 검색 시 tag가 blank이면 예외가 발생한다")
    void searchClubsByTag_BlankTag() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> clubService.searchClubsByTag("   ", pageRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag cannot be null or blank");
    }

    @Test
    @DisplayName("태그 검색 시 tag가 30자를 초과하면 예외가 발생한다")
    void searchClubsByTag_TagTooLong() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        String longTag = "a".repeat(31);

        // when & then: TagNormalizer.normalize()가 null 반환 → "Tag cannot be null or blank"
        assertThatThrownBy(() -> clubService.searchClubsByTag(longTag, pageRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag cannot be null or blank");
    }

    @Test
    @DisplayName("태그 검색 시 Pageable이 null이면 예외가 발생한다")
    void searchClubsByTag_NullPageable() {
        // when & then
        assertThatThrownBy(() -> clubService.searchClubsByTag("coding", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pageable cannot be null");
    }

    @Test
    @DisplayName("태그 검색 시 정확히 30자 태그는 허용된다")
    void searchClubsByTag_TagExactly30Chars() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        String exactTag = "a".repeat(30);
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of(club)));
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));
        given(clubRepository.findLikeCountsByIds(List.of(1L))).willReturn(List.of());

        // when
        Page<ClubSummaryResponse> clubs = clubService.searchClubsByTag(exactTag, pageRequest);

        // then
        assertThat(clubs.getContent()).hasSize(1);
        verify(clubRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("태그 검색 시 31자 태그는 거부된다")
    void searchClubsByTag_TagExactly31Chars() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        String longTag = "a".repeat(31);

        // when & then: TagNormalizer.normalize()가 null 반환 → "Tag cannot be null or blank"
        assertThatThrownBy(() -> clubService.searchClubsByTag(longTag, pageRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag cannot be null or blank");
    }

    @Test
    @DisplayName("태그 검색 시 # 접두어가 제거된 후 정규화된 값으로 검색된다")
    void searchClubsByTag_RemovesHash() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        // "#운동" → normalize → "운동" → Specification 검색
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of(club)));
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));
        given(clubRepository.findLikeCountsByIds(List.of(1L))).willReturn(List.of());

        // when
        Page<ClubSummaryResponse> result = clubService.searchClubsByTag("#운동", pageRequest);

        // then: # 제거 후 Specification 기반으로 검색됨
        assertThat(result.getContent()).hasSize(1);
        verify(clubRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("태그 검색 시 #만 입력되면 예외가 발생한다")
    void searchClubsByTag_HashOnly() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when & then: "###" → normalize → "" (blank) → null → "Tag cannot be null or blank"
        assertThatThrownBy(() -> clubService.searchClubsByTag("###", pageRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag cannot be null or blank");
    }


    private ClubCreateRequest createCreateRequest(String slug) {
        return ClubCreateRequest.builder()
                .name("Name")
                .authorId(1L)
                .categoryId(1L)
                .slug(slug)
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .hasFee(false)
                .isPublic(true)
                .tags(List.of("운동"))
                .build();
    }

    private ClubUpdateRequest createUpdateRequest() {
        return ClubUpdateRequest.builder()
                .name("New Name")
                .summary("New Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .hasFee(false)
                .isPublic(true)
                .tags(List.of("운동"))
                .build();
    }

    // ── resolveAndSetTags ─────────────────────────────────────────────────────

    @Test
    @DisplayName("태그 목록이 null이면 예외가 발생한다")
    void resolveAndSetTags_NullTags() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when & then
        assertThatThrownBy(() -> clubService.updateClub(1L,
                ClubUpdateRequest.builder()
                        .name("Name").summary("Summary")
                        .recruitingStatus(RecruitingStatus.OPEN)
                        .hasFee(false).isPublic(true)
                        .tags(null)
                        .build()))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ValidationErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("태그 목록이 빈 리스트이면 예외가 발생한다")
    void resolveAndSetTags_EmptyTags() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when & then
        assertThatThrownBy(() -> clubService.updateClub(1L,
                ClubUpdateRequest.builder()
                        .name("Name").summary("Summary")
                        .recruitingStatus(RecruitingStatus.OPEN)
                        .hasFee(false).isPublic(true)
                        .tags(List.of())
                        .build()))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ValidationErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("모든 태그가 정규화 후 유효하지 않으면 예외가 발생한다")
    void resolveAndSetTags_AllTagsInvalidAfterNormalization() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when & then: "###" → normalize → null, "   " → normalize → null
        assertThatThrownBy(() -> clubService.updateClub(1L,
                ClubUpdateRequest.builder()
                        .name("Name").summary("Summary")
                        .recruitingStatus(RecruitingStatus.OPEN)
                        .hasFee(false).isPublic(true)
                        .tags(List.of("###", "   "))
                        .build()))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ValidationErrorCode.INVALID_INPUT);
    }

    // ── searchClubsByName ─────────────────────────────────────────────────────

    @Test
    @DisplayName("이름으로 동아리를 검색한다 (3자 이하 → ILIKE 경로)")
    void searchClubsByName_ShortKeyword() {
        // given: 3자 키워드 → findIdsByNameIlike 호출
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findIdsByNameIlike("축구", 10, 0)).willReturn(List.of(1L));
        given(clubRepository.countByNameIlike("축구")).willReturn(1L);
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));

        // when
        Page<ClubSummaryResponse> result = clubService.searchClubsByName("축구", pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(clubRepository).findIdsByNameIlike("축구", 10, 0);
        verify(clubRepository, org.mockito.Mockito.never()).findIdsByNameTrgm(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이름으로 동아리를 검색한다 (4자 이상 → trigram 경로)")
    void searchClubsByName_LongKeyword() {
        // given: 4자 키워드 → findIdsByNameTrgm 호출
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findIdsByNameTrgm("축구동아리", 10, 0)).willReturn(List.of(1L));
        given(clubRepository.countByNameTrgm("축구동아리")).willReturn(1L);
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));

        // when
        Page<ClubSummaryResponse> result = clubService.searchClubsByName("축구동아리", pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(clubRepository).findIdsByNameTrgm("축구동아리", 10, 0);
        verify(clubRepository, org.mockito.Mockito.never()).findIdsByNameIlike(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("이름 검색 결과가 없으면 빈 페이지를 반환한다")
    void searchClubsByName_NoResults() {
        // given: "없는동아리"는 5자 → trigram 경로
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findIdsByNameTrgm("없는동아리", 10, 0)).willReturn(List.of());
        given(clubRepository.countByNameTrgm("없는동아리")).willReturn(0L);

        // when
        Page<ClubSummaryResponse> result = clubService.searchClubsByName("없는동아리", pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(clubRepository, org.mockito.Mockito.never()).findAllByIdInWithGraph(any());
    }

    @Test
    @DisplayName("이름 검색 시 공백 키워드는 거부된다")
    void searchClubsByName_BlankKeyword() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> clubService.searchClubsByName("   ", pageRequest))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ValidationErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("이름 검색 시 null 키워드는 거부된다")
    void searchClubsByName_NullKeyword() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> clubService.searchClubsByName(null, pageRequest))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ValidationErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("이름 검색 키워드 앞뒤 공백은 제거 후 검색된다")
    void searchClubsByName_TrimsKeyword() {
        // given: "  축구  " → strip → "축구" (2자, ILIKE 경로)
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findIdsByNameIlike("축구", 10, 0)).willReturn(List.of(1L));
        given(clubRepository.countByNameIlike("축구")).willReturn(1L);
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));

        // when
        clubService.searchClubsByName("  축구  ", pageRequest);

        // then: 공백이 제거된 "축구"로 Repository가 호출됨
        verify(clubRepository).findIdsByNameIlike("축구", 10, 0);
    }

    // ── filterClubs ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("name 단독 필터는 searchClubsByName으로 위임된다")
    void filterClubs_NameOnly_DelegatesToSearchClubsByName() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        ClubFilterRequest filter = new ClubFilterRequest(null, null, null, null, "축구", null);
        given(clubRepository.findIdsByNameIlike("축구", 10, 0)).willReturn(List.of(1L));
        given(clubRepository.countByNameIlike("축구")).willReturn(1L);
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));

        // when
        Page<ClubSummaryResponse> result = clubService.filterClubs(filter, pageRequest);

        // then: trigram/ILIKE 최적화 경로 사용
        assertThat(result.getContent()).hasSize(1);
        verify(clubRepository).findIdsByNameIlike("축구", 10, 0);
    }

    @Test
    @DisplayName("필터가 모두 비어 있으면 isPublic Specification만 적용된 전체 공개 목록을 반환한다")
    void filterClubs_Empty_ReturnsAllPublicViaSpecification() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        ClubFilterRequest filter = new ClubFilterRequest(null, null, null, null, null, null);
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of(club)));
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));
        given(clubRepository.findLikeCountsByIds(List.of(1L))).willReturn(List.of());

        // when
        Page<ClubSummaryResponse> result = clubService.filterClubs(filter, pageRequest);

        // then: Specification 경로로 처리됨 (name 단독 경로 미사용)
        assertThat(result.getContent()).hasSize(1);
        verify(clubRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest));
        verify(clubRepository, org.mockito.Mockito.never()).findIdsByNameIlike(any(), anyInt(), anyInt());
        verify(clubRepository, org.mockito.Mockito.never()).findIdsByNameTrgm(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("빈 categoryIds는 필터 없음으로 처리되어 전체 공개 목록을 반환한다")
    void filterClubs_EmptyCategoryIds_TreatedAsNoFilter() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        ClubFilterRequest filter = new ClubFilterRequest(List.of(), null, null, null, null, null);
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of(club)));
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));
        given(clubRepository.findLikeCountsByIds(List.of(1L))).willReturn(List.of());

        // when
        Page<ClubSummaryResponse> result = clubService.filterClubs(filter, pageRequest);

        // then: 빈 categoryIds는 hasCategories()가 null 반환 → Specification에서 무시됨
        assertThat(result.getContent()).hasSize(1);
        verify(clubRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("tag + 다른 필터 조합은 Specification 경로를 사용한다")
    void filterClubs_TagWithFilters_UsesSpecification() {
        // given: tag + hasFee 조합
        PageRequest pageRequest = PageRequest.of(0, 10);
        ClubFilterRequest filter = new ClubFilterRequest(null, null, true, null, null, "운동");
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of(club)));
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));
        given(clubRepository.findLikeCountsByIds(List.of(1L))).willReturn(List.of());

        // when
        Page<ClubSummaryResponse> result = clubService.filterClubs(filter, pageRequest);

        // then: Specification으로 처리됨 (findIdsByNameIlike 미호출)
        assertThat(result.getContent()).hasSize(1);
        verify(clubRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest));
        verify(clubRepository, org.mockito.Mockito.never()).findIdsByNameIlike(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("name + tag 조합은 Specification AND 조건으로 처리된다")
    void filterClubs_NameAndTag_UsesSpecificationAndCondition() {
        // given: name + tag → hasNameLike AND hasTag Specification
        PageRequest pageRequest = PageRequest.of(0, 10);
        ClubFilterRequest filter = new ClubFilterRequest(null, null, null, null, "축구", "운동");
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of(club)));
        given(clubRepository.findAllByIdInWithGraph(List.of(1L))).willReturn(List.of(club));
        given(clubRepository.findLikeCountsByIds(List.of(1L))).willReturn(List.of());

        // when
        Page<ClubSummaryResponse> result = clubService.filterClubs(filter, pageRequest);

        // then: trigram 경로(ILIKE)가 아닌 Specification 경로 사용
        verify(clubRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest));
        verify(clubRepository, org.mockito.Mockito.never()).findIdsByNameIlike(any(), anyInt(), anyInt());
        verify(clubRepository, org.mockito.Mockito.never()).findIdsByNameTrgm(any(), anyInt(), anyInt());
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Specification 결과가 없으면 빈 페이지를 반환한다")
    void filterClubs_SpecificationNoResults() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        ClubFilterRequest filter = new ClubFilterRequest(null, null, true, null, null, null);
        given(clubRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageRequest)))
                .willReturn(new PageImpl<>(List.of()));

        // when
        Page<ClubSummaryResponse> result = clubService.filterClubs(filter, pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();
        verify(clubRepository, org.mockito.Mockito.never()).findAllByIdInWithGraph(any());
    }

    @Test
    @DisplayName("좋아요를 누르지 않은 동아리에 좋아요를 누륾면 좋아요가 추가된다")
    void toggleLike_Add() {
        // given
        given(clubRepository.existsById(1L)).willReturn(true);
        given(userService.existsById(2L)).willReturn(true);
        given(clubRepository.addLikeAtomic(2L, 1L)).willReturn(1);

        // when
        boolean isLiked = clubService.toggleLike(1L, 2L);

        // then
        assertThat(isLiked).isTrue();
        verify(clubRepository).addLikeAtomic(2L, 1L);
    }

    @Test
    @DisplayName("이미 좋아요를 누른 동아리에 좋아요를 누륾면 좋아요가 취소된다")
    void toggleLike_Remove() {
        // given
        given(clubRepository.existsById(1L)).willReturn(true);
        given(userService.existsById(2L)).willReturn(true);
        given(clubRepository.addLikeAtomic(2L, 1L)).willReturn(0);
        given(clubRepository.removeLikeAtomic(2L, 1L)).willReturn(1);

        // when
        boolean isLiked = clubService.toggleLike(1L, 2L);

        // then
        assertThat(isLiked).isFalse();
        verify(clubRepository).removeLikeAtomic(2L, 1L);
    }

    @Test
    @DisplayName("좋아요 토글 시 존재하지 않는 동아리 ID면 예외가 발생한다")
    void toggleLike_ClubNotFound() {
        // given
        given(clubRepository.existsById(1L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> clubService.toggleLike(1L, 2L))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("좋아요 토글 시 존재하지 않는 사용자 ID면 예외가 발생한다")
    void toggleLike_UserNotFound() {
        // given
        given(clubRepository.existsById(1L)).willReturn(true);
        given(userService.existsById(2L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> clubService.toggleLike(1L, 2L))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }
}
