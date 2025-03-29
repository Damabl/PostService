package org.example.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentNotificationEvent {
    private Long postAuthorId;  // Кому отправлять уведомление
    private Long postId;        // На какой пост оставлен комментарий
    private Long commenterId;   // Кто оставил комментарий
}

