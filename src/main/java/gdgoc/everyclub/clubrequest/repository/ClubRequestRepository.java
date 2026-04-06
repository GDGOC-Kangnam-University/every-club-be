package gdgoc.everyclub.clubrequest.repository;

import gdgoc.everyclub.clubrequest.domain.ClubRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubRequestRepository extends JpaRepository<ClubRequest, Long> {

    @EntityGraph(attributePaths = {"requestedBy", "reviewedBy"})
    List<ClubRequest> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"requestedBy", "reviewedBy"})
    Optional<ClubRequest> findByPublicId(UUID publicId);

    List<ClubRequest> findByRequestedByIdOrderByCreatedAtDesc(Long userId);
}
