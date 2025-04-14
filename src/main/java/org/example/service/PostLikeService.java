package org.example.service;

import lombok.AllArgsConstructor;
import org.example.exception.exceptions.PostNotFoundException;
import org.example.model.entity.Post;
import org.example.properties.TopicProperties;
import org.example.utils.DatabaseContextHolder;
import org.example.utils.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.example.events.LikeNotificationEvent;
import org.example.model.entity.PostLike;
import org.example.repository.PostLikeRepository;
import org.example.repository.PostRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Service
@AllArgsConstructor
public class PostLikeService {
    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final KafkaTemplate<String, LikeNotificationEvent> kafkaTemplate;
    private final TopicProperties topicProperties;
    public boolean toggleLike(Long postId, Long userId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            boolean alreadyLiked = likeRepository.existsByPostIdAndUserId(postId, userId);
            if (alreadyLiked) {
                likeRepository.deleteByPostIdAndUserId(postId, userId);
                return false;
            } else {
                Post post = postRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("Post not found"));
                likeRepository.save(new PostLike(userId, post));

                LikeNotificationEvent event = new LikeNotificationEvent(post.getUserId(), postId, userId);
                kafkaTemplate.send(topicProperties.getLikePost(), event);
                return true;
            }
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    public boolean isPostLikedByUser(Long postId, Long userId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            return likeRepository.existsByPostIdAndUserId(postId, userId);
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    public long getLikesCount(Long postId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            return likeRepository.countByPostId(postId);
        }finally {
            DatabaseContextHolder.clear();
        }
    }
}
