package org.example.service;

import lombok.AllArgsConstructor;
import org.example.properties.TopicProperties;
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
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            likeRepository.save(new PostLike(null,userId ,postRepository.getById(postId)));
            LikeNotificationEvent event = new LikeNotificationEvent(postRepository.findById(postId).get().getUserId(), postId, userId);
            kafkaTemplate.send(topicProperties.getLikePost(), event);
        }
    }

    public void unlikePost(Long postId, Long userId) {
        likeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    public long getLikesCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
