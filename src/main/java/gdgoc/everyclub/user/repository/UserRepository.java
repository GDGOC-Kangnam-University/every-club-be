package gdgoc.everyclub.user.repository;

import gdgoc.everyclub.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
