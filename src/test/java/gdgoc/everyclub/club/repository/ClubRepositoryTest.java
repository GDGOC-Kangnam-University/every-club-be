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

import java.util.List;

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
    @DisplayName("태그로 동아리를 검색한다")
    void findByTagsContaining() {
        // given
        User author = userRepository.save(new User("John Doe", "john@example.com"));
        Category category = categoryRepository.save(new Category("Academic"));
        
        clubRepository.save(Club.builder().name("Club 1").author(author).category(category).slug("slug1").summary("s").tags(List.of("운동", "친목")).isPublic(true).build());
        clubRepository.save(Club.builder().name("Club 2").author(author).category(category).slug("slug2").summary("s").tags(List.of("공부", "GDGOC")).isPublic(true).build());
        clubRepository.save(Club.builder().name("Club 3").author(author).category(category).slug("slug3").summary("s").tags(List.of("친목", "공부")).isPublic(true).build());

        // when
        Page<Club> sportsClubs = clubRepository.findByTagsContaining("운동", PageRequest.of(0, 10));
        Page<Club> studyClubs = clubRepository.findByTagsContaining("공부", PageRequest.of(0, 10));
        Page<Club> hobbyClubs = clubRepository.findByTagsContaining("친목", PageRequest.of(0, 10));

        // then
        assertThat(sportsClubs.getContent()).hasSize(1);
        assertThat(studyClubs.getContent()).hasSize(2);
        assertThat(hobbyClubs.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("slug로 동아리 존재 여부를 확인한다")
    void existsBySlug() {
        // given
        User author = userRepository.save(new User("John Doe", "john@example.com"));
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
        User author = userRepository.save(new User("John Doe", "john@example.com"));
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
