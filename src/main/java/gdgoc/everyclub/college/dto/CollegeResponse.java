package gdgoc.everyclub.college.dto;

import gdgoc.everyclub.college.domain.College;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "학과 목록을 포함한 단과대 응답")
public record CollegeResponse(
        @Schema(description = "단과대 id", example = "1") Long id,
        @Schema(description = "단과대명", example = "공과대학") String name,
        @Schema(description = "소속 학과 목록") List<MajorResponse> majors
) {

    public static CollegeResponse from(College college) {
        return new CollegeResponse(
                college.getId(),
                college.getName(),
                college.getMajors().stream()
                        .map(m -> new MajorResponse(m.getId(), m.getName()))
                        .toList()
        );
    }
}
