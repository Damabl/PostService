package org.example.service;

import org.example.client.UserServiceClient;
import org.example.dto.CommentPreviewDto;
import org.example.dto.UserInfoDto;
import org.example.events.CommentNotificationEvent;
import org.example.model.entity.PostComment;
import org.example.properties.TopicProperties;
import org.example.repository.PostCommentRepository;
import org.example.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private PostCommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private KafkaTemplate<String, CommentNotificationEvent> kafkaTemplate;

    @Mock
    private TopicProperties topicProperties;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private PostCommentService commentService;

    @Test
    void testGetComments() {
        // given
        Long postId = 1L;
        int page = 0;

        PostComment comment = new PostComment();
        comment.setUserId(100L);
        comment.setContent("Test comment");
        comment.setCreatedAt(LocalDateTime.now());
        comment.setReplies(new ArrayList<>());

        Page<PostComment> commentPage = new PageImpl<>(List.of(comment));

        when(commentRepository.findByPostId(eq(postId), any(Pageable.class)))
                .thenReturn(commentPage);

        when(userServiceClient.getUserInfo(100L))
                .thenReturn(new UserInfoDto("user1", 2L));

        // when
        List<CommentPreviewDto> result = commentService.getComments(postId, page);

        // then
        assertEquals(1, result.size());
        CommentPreviewDto dto = result.get(0);
        assertEquals("user1", dto.getUsername());
        assertEquals(2L, dto.getAvatarId());
        assertEquals("Test comment", dto.getContent());
    }

    @Test
    void testGetReplies() {
        // given
        Long commentId = 10L;
        int page = 0;

        PostComment reply = new PostComment();
        reply.setUserId(101L);
        reply.setContent("Reply content");
        reply.setCreatedAt(LocalDateTime.now());
        reply.setReplies(new ArrayList<>());

        Page<PostComment> replyPage = new PageImpl<>(List.of(reply));

        when(commentRepository.findByParentId(eq(commentId), any(Pageable.class)))
                .thenReturn(replyPage);

        when(userServiceClient.getUserInfo(101L))
                .thenReturn(new UserInfoDto("user2", 2L));

        // when
        List<CommentPreviewDto> result = commentService.getReplies(commentId, page);

        // then
        assertEquals(1, result.size());
        CommentPreviewDto dto = result.get(0);
        assertEquals("user2", dto.getUsername());
        assertEquals(2L, dto.getAvatarId());
        assertEquals("Reply content", dto.getContent());
    }
}
