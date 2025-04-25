package org.example.repository;

import org.example.model.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment,Long> {
    Page<PostComment> findByPostId(Long postId, Pageable pageable);
    Page<PostComment> findByParentId(Long parentId,Pageable pageable);
    long countPostCommentById(Long id);
}