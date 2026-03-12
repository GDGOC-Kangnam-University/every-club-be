package gdgoc.everyclub.college.repository;

import gdgoc.everyclub.college.domain.College;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {
}
