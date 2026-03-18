package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClubResponseTest {

    @Test
    @DisplayName("ClubSummaryResponse는 태그 필드를 포함한다")
    void clubSummaryResponse_HasTags() {
        // given
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .summary("Summary")
                .slug("slug")
                .category(category)
                .build();
        club.addTag(Tag.of("운동"));
        club.addTag(Tag.of("친목"));

        // when
        ClubSummaryResponse response = new ClubSummaryResponse(club);

        // then
        assertThat(response.getTags()).containsExactlyInAnyOrder("운동", "친목");
    }

    @Test
    @DisplayName("ClubSummaryResponse는 description 필드를 포함하지 않는다")
    void clubSummaryResponse_NoDescription() {
        // given
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .summary("Summary")
                .description("Detailed Description")
                .slug("slug")
                .category(category)
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build();

        // when
        ClubSummaryResponse response = new ClubSummaryResponse(club);

        // then
        assertThat(response.getName()).isEqualTo("Name");
        assertThat(response.getSummary()).isEqualTo("Summary");
    }

    @Test
    @DisplayName("ClubDetailResponse는 태그 필드를 포함한다")
    void clubDetailResponse_HasTags() {
        // given
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .summary("Summary")
                .description("Detailed Description")
                .slug("slug")
                .category(category)
                .build();
        club.addTag(Tag.of("운동"));
        club.addTag(Tag.of("친목"));

        // when
        ClubDetailResponse response = new ClubDetailResponse(club);

        // then
        assertThat(response.getTags()).containsExactlyInAnyOrder("운동", "친목");
    }

    @Test
    @DisplayName("ClubDetailResponse는 description 필드를 포함한다")
    void clubDetailResponse_HasDescription() {
        // given
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .summary("Summary")
                .description("Detailed Description")
                .slug("slug")
                .category(category)
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build();

        // when
        ClubDetailResponse response = new ClubDetailResponse(club);

        // then
        assertThat(response.getDescription()).isEqualTo("Detailed Description");
    }
}
