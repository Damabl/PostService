package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostPreviewDto {
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private String username;
    private Long avatarId;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;

    public PostPreviewDto(Long postId, Long userId, String title, String content, String username, Long avatarId, Long likeCount, Long commentCount, LocalDateTime createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.username = username;
        this.avatarId = avatarId;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

}
