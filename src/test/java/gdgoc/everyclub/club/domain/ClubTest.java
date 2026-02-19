package gdgoc.everyclub.club.domain;

import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubTest {

    @Test
    @DisplayName("동아리 수정 시 이름과 설명이 변경된다")
    void updateClub() {
        // given
        User author = new User("John Doe", "john@example.com");
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Old Name")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build();
        String newName = "New Name";
        String newSummary = "New Summary";

        // when
        club.update(newName, newSummary, "Desc", null, null, null, RecruitingStatus.OPEN, null, "WEEKLY", false, true);

        // then
        assertThat(club.getName()).isEqualTo(newName);
        assertThat(club.getSummary()).isEqualTo(newSummary);
    }

    @Test
    @DisplayName("동아리 생성 시 이름이 null이면 IllegalArgumentException이 발생한다")
    void createClub_NullName() {
        // given
        User author = new User("John Doe", "john@example.com");
        Category category = new Category("Academic");

        // when & then
        assertThatThrownBy(() -> new Club(null, "Summary", author, category, "slug"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or blank");
    }

    @Test
    @DisplayName("동아리 수정 시 이름을 null로 변경할 수 있다 (DB 저장 시점에 실패)")
    void update_NullName() {
        // given
        User author = new User("John Doe", "john@example.com");
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build();

        // when
        club.update(null, "Summary", "Desc", null, null, null, RecruitingStatus.OPEN, null, "WEEKLY", false, true);

        // then
        assertThat(club.getName()).isNull();
    }

    @Test
    @DisplayName("동아리는 생성 시 빈 좋아요 목록과 0개의 좋아요 수를 가진다")
    void clubHasEmptyLikesByDefault() {
        // given
        User author = new User("John Doe", "john@example.com");
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("Summary")
                .build();

        // then
        assertThat(club.getLikedByUsers()).isEmpty();
        assertThat(club.getLikedByUsers().size()).isZero();
    }

    @Test
    @DisplayName("좋아요를 누른 유저가 추가되면 좋아요 수가 증가한다")
    void getLikeCountIncrementsOnAdd() {
        // given
        User author = new User("John Doe", "john@example.com");
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("Summary")
                .build();
        User user = new User("Liker", "liker@example.com");

        // when
        club.getLikedByUsers().add(user);

        // then
        assertThat(club.getLikedByUsers().size()).isEqualTo(1);
    }
}
