package gdgoc.everyclub.club.repository;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClubRepositoryTest {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("slug로 동아리 존재 여부를 확인한다")
    void existsBySlug() {
        // given
        User author = userRepository.save(User.builder().email("john@example.com").nickname("John Doe").build());
        Category category = categoryRepository.save(new Category("Academic"));
        clubRepository.save(Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug("test-slug")
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build());

        // when
        boolean exists = clubRepository.existsBySlug("test-slug");
        boolean notExists = clubRepository.existsBySlug("non-existent");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("공개된 동아리만 페이징 조회한다")
    void findAllByIsPublicTrue() {
        // given
        User author = userRepository.save(User.builder().email("john@example.com").nickname("John Doe").build());
        Category category = categoryRepository.save(new Category("Academic"));
        clubRepository.save(createClub(author, category, "slug1", true));
        clubRepository.save(createClub(author, category, "slug2", false));
        clubRepository.save(createClub(author, category, "slug3", true));

        // when
        Page<Club> publicClubs = clubRepository.findAllByIsPublicTrue(PageRequest.of(0, 10));

        // then
        assertThat(publicClubs.getContent()).hasSize(2);
        assertThat(publicClubs.getContent()).allMatch(Club::isPublic);
    }

    private Club createClub(User author, Category category, String slug, boolean isPublic) {
        return Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug(slug)
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(isPublic)
                .build();
    }
}
