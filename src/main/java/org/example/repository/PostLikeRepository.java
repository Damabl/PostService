package org.example.repository;

import org.example.model.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike,Long> {
    long countByPostId(Long postId); // Подсчет лайков
    boolean existsByPostIdAndUserId(Long postId, Long userId); // Проверка, лайкал ли уже
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
