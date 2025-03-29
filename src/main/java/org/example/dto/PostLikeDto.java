package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeDto {
    private Long id;

    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;

    @NotNull(message = "ID поста не может быть пустым")
    private Long postId;
}
