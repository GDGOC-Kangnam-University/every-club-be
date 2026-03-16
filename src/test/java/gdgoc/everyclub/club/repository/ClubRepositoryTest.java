package gdgoc.everyclub.club.repository;

import gdgoc.everyclub.club.domain.*;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClubRepositoryTest {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("태그로 동아리를 검색한다 (Tag/ClubTag 조인 기반 정확 매칭)")
    void findByTag_UsingSpecification() {
        // given
        User author = userRepository.save(new User("John Doe", "john@example.com"));
        Category category = categoryRepository.save(new Category("Academic"));

        Tag tagSports = tagRepository.save(Tag.of("운동"));
        Tag tagHobby  = tagRepository.save(Tag.of("친목"));
        Tag tagStudy  = tagRepository.save(Tag.of("공부"));

        Club club1 = clubRepository.save(buildClub("Club 1", "slug1", author, category));
        club1.addTag(tagSports);
        club1.addTag(tagHobby);

        Club club2 = clubRepository.save(buildClub("Club 2", "slug2", author, category));
        club2.addTag(tagStudy);

        Club club3 = clubRepository.save(buildClub("Club 3", "slug3", author, category));
        club3.addTag(tagHobby);
        club3.addTag(tagStudy);

        // when
        Specification<Club> sportSpec = Specification.allOf(
                ClubSpecification.isPublic(), ClubSpecification.hasTag("운동"));
        Specification<Club> studySpec = Specification.allOf(
                ClubSpecification.isPublic(), ClubSpecification.hasTag("공부"));
        Specification<Club> hobbySpec = Specification.allOf(
                ClubSpecification.isPublic(), ClubSpecification.hasTag("친목"));

        Page<Club> sportsClubs = clubRepository.findAll(sportSpec, PageRequest.of(0, 10));
        Page<Club> studyClubs  = clubRepository.findAll(studySpec,  PageRequest.of(0, 10));
        Page<Club> hobbyClubs  = clubRepository.findAll(hobbySpec,  PageRequest.of(0, 10));

        // then
        assertThat(sportsClubs.getContent()).hasSize(1);
        assertThat(studyClubs.getContent()).hasSize(2);
        assertThat(hobbyClubs.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("태그 검색은 # 제거 및 소문자화된 정규화 값으로 정확 매칭한다")
    void findByTag_NormalizationApplied() {
        // given
        User author = userRepository.save(new User("Jane", "jane@example.com"));
        Category category = categoryRepository.save(new Category("Sport"));

        Tag tagGdgoc = tagRepository.save(Tag.of("gdgoc")); // 정규화된 값으로 저장
        Club club = clubRepository.save(buildClub("GDGOC", "gdgoc-slug", author, category));
        club.addTag(tagGdgoc);

        // when: "#GDGOC" 입력 → normalize → "gdgoc" → 정확 매칭
        Specification<Club> spec = Specification.allOf(
                ClubSpecification.isPublic(), ClubSpecification.hasTag("#GDGOC"));
        Page<Club> result = clubRepository.findAll(spec, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("GDGOC");
    }

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

    private Club buildClub(String name, String slug, User author, Category category) {
        return Club.builder()
                .name(name)
                .author(author)
                .category(category)
                .slug(slug)
                .summary("Summary")
                .isPublic(true)
                .build();
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
