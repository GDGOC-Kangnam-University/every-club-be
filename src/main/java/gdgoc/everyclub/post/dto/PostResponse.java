package gdgoc.everyclub.post.dto;

import gdgoc.everyclub.post.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorName = post.getAuthor() != null ? post.getAuthor().getName() : "Unknown";
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}
