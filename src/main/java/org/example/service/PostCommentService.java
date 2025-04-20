package org.example.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;
import org.example.client.UserServiceClient;
import org.example.dto.CommentPreviewDto;
import org.example.dto.PostPreviewDto;
import org.example.dto.UserInfoDto;
import org.example.events.CommentNotificationEvent;
import org.example.exception.exceptions.PostNotFoundException;
import org.example.model.entity.PostComment;
import org.example.properties.TopicProperties;
import org.example.repository.PostCommentRepository;
import org.example.repository.PostRepository;
import org.example.utils.DatabaseContextHolder;
import org.example.utils.DatabaseType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final KafkaTemplate<String,CommentNotificationEvent> kafkaTemplate;
    private final TopicProperties topicProperties;
    private final UserServiceClient userServiceClient;
    public void addComment(Long postId, Long userId, String content, Long parentId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            PostComment comment = new PostComment();
            comment.setPost(postRepository.getById(postId));
            comment.setUserId(userId);
            comment.setContent(content);

            if (parentId != null) {
                comment.setParent(commentRepository.findById(parentId).orElseThrow());
            }
            commentRepository.save(comment);
        }finally {
            DatabaseContextHolder.clear();
        }
        CommentNotificationEvent event = new CommentNotificationEvent(postRepository.findById(postId).orElseThrow(()->new PostNotFoundException("Post not found by"+ postId+"id")).getUserId(), postId, userId);
        kafkaTemplate.send(topicProperties.getCommentPost(), event);

    }

    public List<CommentPreviewDto> getComments(Long postId, int page) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        Pageable pageable = PageRequest.of(page, 3, Sort.by(Sort.Direction.DESC, "id"));
        List<CommentPreviewDto> response = new ArrayList<>();
        CommentPreviewDto commentPreviewDto = new CommentPreviewDto();
        try {
           List<PostComment> postComment= commentRepository.findByPostId(postId,pageable).getContent();
           postComment.forEach(comment->{
               UserInfoDto userInfo = userServiceClient.getUserInfo(comment.getUserId());
               commentPreviewDto.setAvatarId(userInfo.getAvatarId());
               commentPreviewDto.setUsername(userInfo.getUsername());
               commentPreviewDto.setContent(comment.getContent());
               commentPreviewDto.setCreateAt(comment.getCreatedAt());
               commentPreviewDto.setCountReplies(comment.getReplies().size());
               response.add(commentPreviewDto);
           });
           return response;
        }finally {
            DatabaseContextHolder.clear();
        }
    }
    public long getCommentsCount(Long postId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            return commentRepository.countPostCommentById(postId);
        }finally {
            DatabaseContextHolder.clear();
        }
    }
    public List<CommentPreviewDto> getReplies(Long commentId, int page) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        Pageable pageable = PageRequest.of(page, 3, Sort.by(Sort.Direction.DESC, "id"));
        List<CommentPreviewDto> response = new ArrayList<>();
        try {
            List<PostComment> replies = commentRepository.findByParentId(commentId, pageable).getContent();
            replies.forEach(reply -> {
                UserInfoDto userInfo = userServiceClient.getUserInfo(reply.getUserId());
                CommentPreviewDto replyDto = new CommentPreviewDto();
                replyDto.setAvatarId(userInfo.getAvatarId());
                replyDto.setUsername(userInfo.getUsername());
                replyDto.setContent(reply.getContent());
                replyDto.setCreateAt(reply.getCreatedAt());
                replyDto.setCountReplies(reply.getReplies().size());
                response.add(replyDto);
            });
            return response;
        } finally {
            DatabaseContextHolder.clear();
        }
    }

}
