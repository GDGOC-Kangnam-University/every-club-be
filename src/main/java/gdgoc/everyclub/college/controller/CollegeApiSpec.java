package gdgoc.everyclub.college.controller;

import gdgoc.everyclub.college.dto.CollegeResponse;
import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.CollegeDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = CollegeDocs.TAG_NAME, description = CollegeDocs.TAG_DESCRIPTION)
public interface CollegeApiSpec {

    @GetMapping
    @Operation(summary = "단과대 목록 조회", description = "동아리 등록과 필터링에 필요한 단과대와 학과 목록을 조회합니다.")
    ApiResponse<List<CollegeResponse>> getColleges();
}
