package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.dto.ClubCreateRequest;
import gdgoc.everyclub.club.dto.ClubDetailResponse;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ClubService clubService;

    private User author;
    private Category category;
    private Club club;

    @BeforeEach
    void setUp() {
        author = new User("Author", "author@example.com");
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

        // when
        clubService.updateClub(1L, request);

        // then
        assertThat(club.getName()).isEqualTo("New Name");
        assertThat(club.getSummary()).isEqualTo("New Summary");
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
    @DisplayName("태그로 동아리를 검색한다")
    void searchClubsByTag() {
        // given
        String tag = "운동";
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findByTagsContaining(tag, pageRequest)).willReturn(new PageImpl<>(List.of(club)));

        // when
        Page<Club> result = clubService.searchClubsByTag(tag, pageRequest);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(club);
        verify(clubRepository).findByTagsContaining(tag, pageRequest);
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
                .build();
    }
}
