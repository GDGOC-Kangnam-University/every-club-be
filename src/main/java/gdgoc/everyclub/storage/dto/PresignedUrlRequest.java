package gdgoc.everyclub.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업로드용 presigned URL 발급 요청")
public class PresignedUrlRequest {
    @Schema(description = "원본 파일명", example = "club-banner.png")
    @NotBlank(message = "File name is required")
    private String fileName;

    @Schema(description = "파일 Content-Type", example = "image/png")
    @NotBlank(message = "Content type is required")
    private String contentType;
}
