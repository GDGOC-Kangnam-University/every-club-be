package gdgoc.everyclub.club.repository;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.ClubSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ClubRepository extends JpaRepository<Club, Long> {
    @EntityGraph(attributePaths = "author")
    Page<Club> findAll(Pageable pageable);

    @Query("select p from Club p join fetch p.author join fetch p.category where p.id = :id")
    Optional<Club> findByIdWithAuthor(@Param("id") Long id);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Club> findAllByIsPublicTrue(Pageable pageable);

    @Query("SELECT new gdgoc.everyclub.club.dto.ClubSummaryResponse(c, CAST(COUNT(u) AS int)) " +
           "FROM Club c LEFT JOIN c.likedByUsers u " +
           "WHERE c.isPublic = true " +
           "GROUP BY c.id, c.slug, c.name, c.summary, c.logoUrl, c.recruitingStatus, c.activityCycle, c.hasFee, c.category, c.author, c.createdAt, c.updatedAt")
    Page<ClubSummaryResponse> findAllPublicWithLikeCounts(Pageable pageable);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.likedClubs c WHERE u.id = :userId AND c.id = :clubId")
    boolean existsLikeByUserIdAndClubId(@Param("userId") Long userId, @Param("clubId") Long clubId);

    @Query("SELECT COUNT(u) FROM User u JOIN u.likedClubs c WHERE c.id = :clubId")
    int countLikesByClubId(@Param("clubId") Long clubId);

    /**
     * Atomically adds a like entry. Returns 1 if inserted, 0 if already exists.
     * Uses native query with ON CONFLICT DO NOTHING for PostgreSQL to handle race conditions.
     */
    @Modifying
    @Query(value = "INSERT INTO club_likes (user_id, club_id) VALUES (:userId, :clubId) " +
                   "ON CONFLICT (user_id, club_id) DO NOTHING",
           nativeQuery = true)
    int addLikeAtomic(@Param("userId") Long userId, @Param("clubId") Long clubId);

    /**
     * Atomically removes a like entry. Returns 1 if deleted, 0 if didn't exist.
     */
    @Modifying
    @Query(value = "DELETE FROM club_likes WHERE user_id = :userId AND club_id = :clubId",
           nativeQuery = true)
    int removeLikeAtomic(@Param("userId") Long userId, @Param("clubId") Long clubId);
}
