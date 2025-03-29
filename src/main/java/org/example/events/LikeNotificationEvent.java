package org.example.events;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeNotificationEvent {
    private Long postAuthorId;  // Кому отправлять уведомление
    private Long postId;        // Какой пост лайкнули
    private Long likerId;       // Кто лайкнул
}
