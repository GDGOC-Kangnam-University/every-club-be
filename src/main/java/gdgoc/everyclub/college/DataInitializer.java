package gdgoc.everyclub.college;

import gdgoc.everyclub.college.domain.College;
import gdgoc.everyclub.college.domain.Major;
import gdgoc.everyclub.college.repository.CollegeRepository;
import gdgoc.everyclub.college.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (collegeRepository.count() > 0) return;

        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put("복지융합대학", List.of("사회복지학부", "시니어비즈니스학과"));
        data.put("경영관리대학", List.of("상경학부", "법행정세무학부"));
        data.put("글로벌문화콘텐츠대학", List.of("문화콘텐츠학과", "국제지역학과", "중국콘텐츠비즈니스학과", "기독교커뮤니케이션학과"));
        data.put("공과대학", List.of("컴퓨터공학부", "인공지능융합공학부", "전자반도체공학부", "부동산건설학부"));
        data.put("예체능대학", List.of("디자인학과", "체육학과", "음악학과"));
        data.put("사범대학", List.of("교육학과", "유아교육과", "초등특수교육과", "중등특수교육과"));

        data.forEach((collegeName, majorNames) -> {
            College college = collegeRepository.save(new College(collegeName));
            majorNames.forEach(majorName ->
                    majorRepository.save(new Major(majorName, college)));
        });
    }
}
