package gdgoc.everyclub.club.domain;

import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubEntityTest {

    @Test
    @DisplayName("동아리 생성 시 추가된 필드들이 올바르게 설정된다")
    void createClubWithNewFields() {
        // given
        User author = User.builder().email("john@example.com").nickname("John Doe").build();
        Category category = new Category("Academic");
        String slug = "gdg-on-campus";
        String summary = "GDG on Campus club";
        String description = "This is a detailed description of the club.";
        String logoUrl = "https://example.com/logo.png";
        String bannerUrl = "https://example.com/banner.png";
        String joinFormUrl = "https://example.com/join";
        RecruitingStatus recruitingStatus = RecruitingStatus.OPEN;
        String department = "Computer Science";
        String activityCycle = "WEEKLY";
        boolean hasFee = true;
        boolean isPublic = true;

        // when
        Club club = Club.builder()
                .name("GDGOC")
                .author(author)
                .category(category)
                .slug(slug)
                .summary(summary)
                .description(description)
                .logoUrl(logoUrl)
                .bannerUrl(bannerUrl)
                .joinFormUrl(joinFormUrl)
                .recruitingStatus(recruitingStatus)
                .department(department)
                .activityCycle(activityCycle)
                .hasFee(hasFee)
                .isPublic(isPublic)
                .build();

        // then
        assertThat(club.getName()).isEqualTo("GDGOC");
        assertThat(club.getSlug()).isEqualTo(slug);
        assertThat(club.getSummary()).isEqualTo(summary);
        assertThat(club.getDescription()).isEqualTo(description);
        assertThat(club.getLogoUrl()).isEqualTo(logoUrl);
        assertThat(club.getBannerUrl()).isEqualTo(bannerUrl);
        assertThat(club.getJoinFormUrl()).isEqualTo(joinFormUrl);
        assertThat(club.getRecruitingStatus()).isEqualTo(recruitingStatus);
        assertThat(club.getDepartment()).isEqualTo(department);
        assertThat(club.getActivityCycle()).isEqualTo(activityCycle);
        assertThat(club.isHasFee()).isEqualTo(hasFee);
        assertThat(club.isPublic()).isEqualTo(isPublic);
        assertThat(club.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("생성자를 통해 동아리를 생성한다")
    void createClubWithConstructor() {
        // given
        User author = User.builder().email("john@example.com").nickname("John Doe").build();
        Category category = new Category("Academic");
        String slug = "slug";
        String name = "Club Name";
        String summary = "Summary";

        // when
        Club club = new Club(name, summary, author, category, slug);

        // then
        assertThat(club.getName()).isEqualTo(name);
        assertThat(club.getSummary()).isEqualTo(summary);
        assertThat(club.getAuthor()).isEqualTo(author);
        assertThat(club.getCategory()).isEqualTo(category);
        assertThat(club.getSlug()).isEqualTo(slug);
        assertThat(club.getRecruitingStatus()).isEqualTo(RecruitingStatus.OPEN);
        assertThat(club.isHasFee()).isFalse();
        assertThat(club.isPublic()).isFalse();
        assertThat(club.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("동아리 생성 시 이름이 null이면 IllegalArgumentException이 발생한다")
    void createClub_NullName() {
        // given
        User author = User.builder().email("john@example.com").nickname("John Doe").build();
        Category category = new Category("Academic");

        // when & then
        assertThatThrownBy(() -> new Club(null, "Summary", author, category, "slug"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or blank");
    }

    @Test
    @DisplayName("동아리 생성 시 이름이 공백이면 IllegalArgumentException이 발생한다")
    void createClub_BlankName() {
        // given
        User author = User.builder().email("john@example.com").nickname("John Doe").build();
        Category category = new Category("Academic");

        // when & then
        assertThatThrownBy(() -> new Club("  ", "Summary", author, category, "slug"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or blank");
    }
}
