package gdgoc.everyclub.college.service;

import gdgoc.everyclub.college.dto.CollegeResponse;
import gdgoc.everyclub.college.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollegeService {

    private final CollegeRepository collegeRepository;

    public List<CollegeResponse> getAllColleges() {
        return collegeRepository.findAll().stream()
                .map(CollegeResponse::from)
                .toList();
    }
}
