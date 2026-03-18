package gdgoc.everyclub.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업로드용 presigned URL 응답")
public class PresignedUrlResponse {
    @Schema(description = "발급된 presigned URL", example = "https://storage.googleapis.com/bucket/object?signature=abc123")
    private String presignedUrl;
}
