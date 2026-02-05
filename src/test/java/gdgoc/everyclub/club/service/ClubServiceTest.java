package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.ClubCreateRequest;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.repository.ClubRepository;
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
    private UserService userService;

    @InjectMocks
    private ClubService clubService;

    private User author;
    private Club club;

    @BeforeEach
    void setUp() {
        author = new User("Author", "author@example.com");
        ReflectionTestUtils.setField(author, "id", 1L);

        club = new Club("Title", "Content", author);
        ReflectionTestUtils.setField(club, "id", 1L);
    }

    @Test
    @DisplayName("동아리를 생성한다")
    void createClub() {
        // given
        ClubCreateRequest request = new ClubCreateRequest("Title", "Content", 1L);
        given(userService.getUserById(1L)).willReturn(author);
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
    @DisplayName("동아리 생성 시 존재하지 않는 작성자 ID면 예외가 발생한다")
    void createClub_AuthorNotFound() {
        // given
        ClubCreateRequest request = new ClubCreateRequest("Title", "Content", 999L);
        given(userService.getUserById(999L))
                .willThrow(new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

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
    @DisplayName("동아리를 전체 조회한다")
    void getClubs() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(clubRepository.findAll(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(club)));

        // when
        Page<Club> clubs = clubService.getClubs(pageRequest);

        // then
        assertThat(clubs.getContent()).hasSize(1);
        assertThat(clubs.getContent().get(0).getTitle()).isEqualTo("Title");
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
        ClubUpdateRequest request = new ClubUpdateRequest("New Title", "New Content");
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when
        clubService.updateClub(1L, request);

        // then
        assertThat(club.getTitle()).isEqualTo("New Title");
        assertThat(club.getContent()).isEqualTo("New Content");
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
    @DisplayName("동아리를 삭제한다")
    void deleteClub() {
        // given
        given(clubRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(club));

        // when
        clubService.deleteClub(1L);

        // then
        verify(clubRepository).delete(club);
    }
}
