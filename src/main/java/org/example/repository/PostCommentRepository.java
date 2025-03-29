package org.example.repository;

import org.example.model.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment,Long> {
    List<PostComment> findByPostId(Long postId); // Все комментарии к посту
    List<PostComment> findByParentId(Long parentId);
}