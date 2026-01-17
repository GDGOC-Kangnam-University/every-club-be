package gdgoc.everyclub.post.domain;

import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("게시글 수정 시 제목과 내용이 변경된다")
    void updatePost() {
        // given
        User author = new User("John Doe", "john@example.com");
        Post post = new Post("Old Title", "Old Content", author);
        String newTitle = "New Title";
        String newContent = "New Content";

        // when
        post.update(newTitle, newContent);

        // then
        assertThat(post.getTitle()).isEqualTo(newTitle);
        assertThat(post.getContent()).isEqualTo(newContent);
    }
}
