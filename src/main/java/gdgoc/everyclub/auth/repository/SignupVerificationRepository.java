package gdgoc.everyclub.auth.repository;

import gdgoc.everyclub.auth.domain.SignupVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignupVerificationRepository extends JpaRepository<SignupVerification, Long> {

    Optional<SignupVerification> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    Optional<SignupVerification> findBySignupTokenHash(String signupTokenHash);

    void deleteByEmail(String email);
}