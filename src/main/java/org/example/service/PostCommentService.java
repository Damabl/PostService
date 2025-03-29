package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.example.events.CommentNotificationEvent;
import org.example.exception.exceptions.PostNotFoundException;
import org.example.model.entity.PostComment;
import org.example.properties.TopicProperties;
import org.example.repository.PostCommentRepository;
import org.example.repository.PostRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final KafkaTemplate<String,CommentNotificationEvent> kafkaTemplate;
    private final TopicProperties topicProperties;

    public void addComment(Long postId, Long userId, String content, Long parentId) {
        PostComment comment = new PostComment();
        comment.setPost(postRepository.getById(postId));
        comment.setUserId(userId);
        comment.setContent(content);

        if (parentId != null) {
            comment.setParent(commentRepository.findById(parentId).orElseThrow());
        }

        commentRepository.save(comment);
        CommentNotificationEvent event = new CommentNotificationEvent(postRepository.findById(postId).orElseThrow(()->new PostNotFoundException("Post not found by"+ postId+"id")).getUserId(), postId, userId);
        kafkaTemplate.send(topicProperties.getCommentPost(), event);
    }

    public List<PostComment> getComments(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public List<PostComment> getReplies(Long commentId) {
        return commentRepository.findByParentId(commentId);
    }
}
