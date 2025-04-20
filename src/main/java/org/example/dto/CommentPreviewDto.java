package org.example.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentPreviewDto {
    private String content;
    private String username;
    private Long avatarId;
    private LocalDateTime createAt;
    private int countReplies;
}
