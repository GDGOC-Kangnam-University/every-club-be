package gdgoc.everyclub.post.repository;

import gdgoc.everyclub.post.domain.Post;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.EntityGraph;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;



import java.util.Optional;



public interface PostRepository extends JpaRepository<Post, Long> {



    @EntityGraph(attributePaths = "author")

    Page<Post> findAll(Pageable pageable);



    @Query("select p from Post p join fetch p.author where p.id = :id")

    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

}


