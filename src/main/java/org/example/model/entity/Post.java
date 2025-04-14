package org.example.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.kafka.common.protocol.types.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Table(name = "post")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "content", nullable = false)
    private String content;

    @ManyToMany
    @JoinTable(
            name = "post_images",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    private List<Image> images;

    @OneToMany(mappedBy = "post")
    private List<PostLike> postLikeList;

    @OneToMany(mappedBy = "post")
    private List<PostComment> postCommentList;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_categories", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "category_id")
    private List<String> categories;
    private LocalDateTime createdAt;

    public Post(Long id, Long userId, String title) {
        this.id=id;
        this.userId=userId;
        this.title=title;
    }
    public Post(Long id, Long userId, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

}
