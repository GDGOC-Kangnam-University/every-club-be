package gdgoc.everyclub.college.controller;

import gdgoc.everyclub.college.dto.CollegeResponse;
import gdgoc.everyclub.college.service.CollegeService;
import gdgoc.everyclub.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/colleges")
@RequiredArgsConstructor
@Tag(name = "Colleges", description = "단과대/학과 조회 API")
public class CollegeController {

    private final CollegeService collegeService;

    @GetMapping
    @Operation(summary = "단과대 목록 조회", description = "동아리 등록과 필터링에 필요한 단과대와 학과 목록을 조회합니다.")
    public ApiResponse<List<CollegeResponse>> getColleges() {
        return ApiResponse.success(collegeService.getAllColleges());
    }
}
