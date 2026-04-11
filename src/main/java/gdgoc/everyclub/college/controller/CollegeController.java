package gdgoc.everyclub.college.controller;

import gdgoc.everyclub.college.dto.CollegeResponse;
import gdgoc.everyclub.college.service.CollegeService;
import gdgoc.everyclub.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/colleges")
@RequiredArgsConstructor
public class CollegeController implements CollegeApiSpec {

    private final CollegeService collegeService;

    @Override
    public ApiResponse<List<CollegeResponse>> getColleges() {
        return ApiResponse.success(collegeService.getAllColleges());
    }
}
