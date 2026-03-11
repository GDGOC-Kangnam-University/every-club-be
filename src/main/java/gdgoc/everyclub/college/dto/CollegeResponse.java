package gdgoc.everyclub.college.dto;

import gdgoc.everyclub.college.domain.College;

import java.util.List;

public record CollegeResponse(Long id, String name, List<MajorResponse> majors) {

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
