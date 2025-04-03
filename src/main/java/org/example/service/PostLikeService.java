package org.example.service;

import lombok.AllArgsConstructor;
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
    public void likePost(Long postId, Long userId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
                likeRepository.save(new PostLike(null, userId, postRepository.getById(postId)));
                LikeNotificationEvent event = new LikeNotificationEvent(postRepository.findById(postId).get().getUserId(), postId, userId);
                kafkaTemplate.send(topicProperties.getLikePost(), event);
            }
        }finally {
            DatabaseContextHolder.clear();
        }
    }

    public void unlikePost(Long postId, Long userId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            likeRepository.deleteByPostIdAndUserId(postId, userId);
        }finally {
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
