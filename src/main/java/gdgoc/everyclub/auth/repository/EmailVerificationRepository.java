package gdgoc.everyclub.auth.repository;

import gdgoc.everyclub.auth.domain.EmailVerification;
import gdgoc.everyclub.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByUser(User user);

    @Modifying
    void deleteByUser(User user);
}