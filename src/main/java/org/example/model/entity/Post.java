package org.example.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.kafka.common.protocol.types.Field;

import java.io.Serializable;
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
    @Column(name = "title")
    private String title;
    @Column(name = "content")
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
}
