package gdgoc.everyclub.college.repository;

import gdgoc.everyclub.college.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major, Long> {
    List<Major> findByCollegeId(Long collegeId);
}
