package gdgoc.everyclub.clubrequest.repository;

import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClubRequestRepository extends JpaRepository<ClubRequest, Long> {
    Optional<ClubRequest> findByPublicId(UUID publicId);
}
