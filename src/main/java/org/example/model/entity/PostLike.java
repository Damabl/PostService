package org.example.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="post_like")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {
    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private Post post;
    public PostLike(Long userId, Post post) {
        this.userId = userId;
        this.post = post;
    }
}

