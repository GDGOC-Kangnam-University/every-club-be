package gdgoc.everyclub.club.repository;

import gdgoc.everyclub.club.domain.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}


