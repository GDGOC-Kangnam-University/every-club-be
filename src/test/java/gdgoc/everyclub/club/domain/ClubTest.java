package gdgoc.everyclub.club.domain;

import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubTest {

    @Test
    @DisplayName("게시글 수정 시 제목과 내용이 변경된다")
    void updatePost() {
        // given
        User author = new User("John Doe", "john@example.com");
        Club club = new Club("Old Title", "Old Content", author);
        String newTitle = "New Title";
        String newContent = "New Content";

        // when
        club.update(newTitle, newContent);

        // then
        assertThat(club.getTitle()).isEqualTo(newTitle);
        assertThat(club.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("게시글 생성 시 제목이 null이면 IllegalArgumentException이 발생한다")
    void createPost_NullTitle() {
        // when & then
        assertThatThrownBy(() -> new Club(null, "Content", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title cannot be null or blank");
    }

    @Test
    @DisplayName("게시글 수정 시 제목을 null로 변경할 수 있다 (DB 저장 시점에 실패)")
    void update_NullTitle() {
        // given
        User author = new User("John Doe", "john@example.com");
        Club club = new Club("Title", "Content", author);

        // when
        club.update(null, "New Content");

        // then
        assertThat(club.getTitle()).isNull();
    }
}
