package gdgoc.everyclub.club.repository;

import gdgoc.everyclub.club.domain.ClubAdmin;
import gdgoc.everyclub.club.domain.ClubAdminRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubAdminRepository extends JpaRepository<ClubAdmin, Long> {

    boolean existsByUserIdAndClubId(Long userId, Long clubId);

    boolean existsByUserIdAndClubIdAndRole(Long userId, Long clubId, ClubAdminRole role);

    @EntityGraph(attributePaths = {"club", "club.category", "club.major", "club.clubTags.tag"})
    List<ClubAdmin> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<ClubAdmin> findByClubId(Long clubId);

    Optional<ClubAdmin> findByUserIdAndClubId(Long userId, Long clubId);
}