package gdgoc.everyclub.post.repository;

import gdgoc.everyclub.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select p from Post p join fetch p.author",
            countQuery = "select count(p) from Post p")
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query("select p from Post p join fetch p.author")
    List<Post> findAllWithAuthor();
}
