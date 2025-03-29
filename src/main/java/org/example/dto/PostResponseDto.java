package org.example.dto;

import lombok.Data;

@Data
public class PostResponseDto {
    private Long postId;
    private Long userId;
    private String content;
    private String firstName;
    private String lastName;
    private String imageData;
    private String avatarData;

    public PostResponseDto(Long postId, Long userId, String content, String firstName,String lastName, String imageData, String avatarData) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageData = imageData;
        this.avatarData = avatarData;
    }

}
