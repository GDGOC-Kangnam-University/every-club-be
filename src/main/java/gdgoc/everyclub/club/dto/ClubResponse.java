package gdgoc.everyclub.club.dto;

import gdgoc.everyclub.club.domain.Club;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ClubResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ClubResponse(Club club) {
        this.id = club.getId();
        this.title = club.getTitle();
        this.content = club.getContent();
        this.authorName = club.getAuthor() != null ? club.getAuthor().getName() : "Unknown";
        this.createdAt = club.getCreatedAt();
        this.updatedAt = club.getUpdatedAt();
    }
}
