package gdgoc.everyclub.college.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학과 정보")
public record MajorResponse(
        @Schema(description = "학과 id", example = "101") Long id,
        @Schema(description = "학과명", example = "컴퓨터공학과") String name
) {
}
