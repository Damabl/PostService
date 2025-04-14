package org.example.repository;

import org.example.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAll();
    Optional<Post> findById(Long id);
    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c IN :categories")
    Page<Post> findByCategories(@Param("categories") List<String> categoryIds, Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword%")
    Page<Post> findByContentContaining(@Param("keyword") String keyword, Pageable pageable);
}
