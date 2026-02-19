package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.ClubDetailResponse;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClubLikeIntegrationTest {

    @Autowired
    private ClubService clubService;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("좋아요를 토글하면 동아리 상세 정보의 좋아요 상태와 개수가 업데이트된다")
    void toggleLike_Integration() {
        // given
        User user = userRepository.save(new User("Liker", "liker@example.com"));
        User author = userRepository.save(new User("Author", "author@example.com"));
        Category category = categoryRepository.save(new Category("Academic"));
        
        Club club = clubRepository.save(Club.builder()
                .name("Test Club")
                .author(author)
                .category(category)
                .slug("test-club")
                .summary("Summary")
                .isPublic(true)
                .build());

        // when: 좋아요 추가
        boolean liked = clubService.toggleLike(club.getId(), user.getId());

        // then: 상태 확인
        assertThat(liked).isTrue();
        ClubDetailResponse response = clubService.getPublicClubById(club.getId(), user.getId());
        assertThat(response.isLiked()).isTrue();
        assertThat(response.getLikeCount()).isEqualTo(1);

        // when: 좋아요 취소
        boolean unliked = clubService.toggleLike(club.getId(), user.getId());

        // then: 상태 확인
        assertThat(unliked).isFalse();
        response = clubService.getPublicClubById(club.getId(), user.getId());
        assertThat(response.isLiked()).isFalse();
        assertThat(response.getLikeCount()).isZero();
    }
}
