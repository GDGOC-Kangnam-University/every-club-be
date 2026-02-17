package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
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
        List<String> tags = List.of("운동", "친목");
        Club club = Club.builder()
                .name("Name")
                .summary("Summary")
                .slug("slug")
                .category(category)
                .tags(tags)
                .build();

        // when
        ClubSummaryResponse response = new ClubSummaryResponse(club);

        // then
        assertThat(response.getTags()).containsExactly("운동", "친목");
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
        List<String> tags = List.of("운동", "친목");
        Club club = Club.builder()
                .name("Name")
                .summary("Summary")
                .description("Detailed Description")
                .slug("slug")
                .category(category)
                .tags(tags)
                .build();

        // when
        ClubDetailResponse response = new ClubDetailResponse(club);

        // then
        assertThat(response.getTags()).containsExactly("운동", "친목");
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
