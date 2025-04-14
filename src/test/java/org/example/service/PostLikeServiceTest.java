package org.example.service;
import org.example.events.LikeNotificationEvent;
import org.example.model.entity.Post;
import org.example.model.entity.PostLike;
import org.example.properties.TopicProperties;
import org.example.repository.PostLikeRepository;
import org.example.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {
    private Post testPost;

    @Mock
    private PostLikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private KafkaTemplate<String, LikeNotificationEvent> kafkaTemplate;

    @Mock
    private TopicProperties topicProperties;

    @InjectMocks
    private PostLikeService postLikeService;
    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setUserId(10L);
    }

    @Test
    void likePost() {
        Long userId = 1L;
        Long postId = 2L;
        when(likeRepository.existsByPostIdAndUserId(postId,userId)).thenReturn(false);
        when(postRepository.getById(postId)).thenReturn(testPost);
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(topicProperties.getLikePost()).thenReturn("like-topic");
        postLikeService.toggleLike(postId, userId);
        verify(likeRepository).save(any(PostLike.class));
        verify(kafkaTemplate).send(eq("like-topic"), any(LikeNotificationEvent.class));
    }

    @Test
    void unlikePost() {
    }

    @Test
    void getLikesCount() {
    }
}