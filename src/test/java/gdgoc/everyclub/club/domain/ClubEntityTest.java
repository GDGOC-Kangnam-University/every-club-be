package gdgoc.everyclub.club.domain;

import gdgoc.everyclub.college.domain.College;
import gdgoc.everyclub.college.domain.Major;
import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClubEntityTest {

    @Test
    @DisplayName("동아리 생성 시 추가된 필드들이 올바르게 설정된다")
    void createClubWithAllFields() {
        // given
        User author = User.builder().email("john@example.com").nickname("John Doe").build();
        Category category = new Category("Academic");
        Major major = new Major("컴퓨터공학부", new College("공과대학"));

        // when
        Club club = Club.builder()
                .name("GDGOC")
                .author(author)
                .category(category)
                .slug("gdg-on-campus")
                .summary("GDG on Campus club")
                .description("Detailed description")
                .logoUrl("https://example.com/logo.png")
                .bannerUrl("https://example.com/banner.png")
                .joinFormUrl("https://example.com/join")
                .recruitingStatus(RecruitingStatus.OPEN)
                .major(major)
                .activityCycle("WEEKLY")
                .hasFee(true)
                .isPublic(true)
                .build();

        // then
        assertThat(club.getName()).isEqualTo("GDGOC");
        assertThat(club.getSlug()).isEqualTo("gdg-on-campus");
        assertThat(club.getSummary()).isEqualTo("GDG on Campus club");
        assertThat(club.getDescription()).isEqualTo("Detailed description");
        assertThat(club.getLogoUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(club.getBannerUrl()).isEqualTo("https://example.com/banner.png");
        assertThat(club.getJoinFormUrl()).isEqualTo("https://example.com/join");
        assertThat(club.getRecruitingStatus()).isEqualTo(RecruitingStatus.OPEN);
        assertThat(club.getMajor()).isEqualTo(major);
        assertThat(club.getActivityCycle()).isEqualTo("WEEKLY");
        assertThat(club.isHasFee()).isTrue();
        assertThat(club.isPublic()).isTrue();
        assertThat(club.getLikedByUsers()).isEmpty();
    }

    @Test
    @DisplayName("addTag()로 태그를 추가하면 getTagNames()로 조회할 수 있다")
    void addTag_AndGetTagNames() {
        // given
        Club club = buildClub();
        Tag tag1 = Tag.of("운동");
        Tag tag2 = Tag.of("친목");

        // when
        club.addTag(tag1);
        club.addTag(tag2);

        // then
        assertThat(club.getTagNames()).containsExactlyInAnyOrder("운동", "친목");
    }

    @Test
    @DisplayName("clearTags()를 호출하면 모든 태그 매핑이 제거된다")
    void clearTags_RemovesAllTagMappings() {
        // given
        Club club = buildClub();
        club.addTag(Tag.of("운동"));
        club.addTag(Tag.of("친목"));

        // when
        club.clearTags();

        // then
        assertThat(club.getTagNames()).isEmpty();
    }

    private Club buildClub() {
        User author = User.builder().email("john@example.com").nickname("John Doe").build();
        Category category = new Category("Academic");
        return Club.builder()
                .name("GDGOC")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("summary")
                .build();
    }
}
