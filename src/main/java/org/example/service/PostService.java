package org.example.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.client.UserServiceClient;
import org.example.dto.PostDto;
import org.example.events.PostCreatedEvent;
import org.example.exception.exceptions.PostNotFoundException;
import org.example.model.entity.Image;
import org.example.model.entity.Post;
import org.example.properties.TopicProperties;
import org.example.repository.PostRepository;
import org.example.utils.DatabaseContextHolder;
import org.example.utils.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final ImageService imageService;
    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;
    private final TopicProperties topicProperties;
    private final UserServiceClient userServiceClient;

    @Value("${image.url}")
    private String urlImage;

    @Transactional
    public String addPost(PostDto postDto) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            Post post = new Post();
            post.setUserId(postDto.getUserId());
            post.setTitle(postDto.getTitle());
            List<Long> imageIds = imageService.uploadMultiple(postDto.getFiles());
            List<Image> images = imageIds.stream().map(imageService::getImageById).toList();
            post.setImages(images);
            post.setContent(replaceNameImages(postDto.getContent(), images));
            postRepository.save(post);
            kafkaTemplate.send(topicProperties.getCommentPost(), new PostCreatedEvent(post.getId(), postDto.getUserId()));
            return "Added";
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<Post> searchPosts(String keyword) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            Pageable pageable = PageRequest.of(0, 20);
            return postRepository.findByContentContaining(keyword, pageable).getContent();
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    @Transactional
    public void updateUserCategories(Long postId, List<String> categoryIds) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found"));
            post.setCategories(categoryIds);
            postRepository.save(post);
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Long userId) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            List<String> categoryIds = userServiceClient.getUserCategories(userId);
            if (categoryIds.isEmpty()) return List.of();
            Pageable pageable = PageRequest.of(0, 20);
            return postRepository.findByCategories(categoryIds, pageable).getContent();
        } finally {
            DatabaseContextHolder.clear();
        }
    }

    public String replaceNameImages(String content, List<Image> images) {
        if (images == null || images.isEmpty()) return content;
        for (int i = 0; i < images.size(); i++) {
            content = content.replace("{image:" + i + "}", "<img src=\"" + urlImage + images.get(i).getName() + "\" alt=\"image\"/>");
        }
        return content;
    }

    @Transactional
    public String deletePost(Long id) throws IOException {
        DatabaseContextHolder.setDatabaseType(DatabaseType.MASTER);
        try {
            Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("Post not found"));
            for (Image image : post.getImages()) {
                imageService.deleteImage(image.getName());
            }
            postRepository.deleteById(id);
            return "Deleted";
        } finally {
            DatabaseContextHolder.clear();
        }
    }
}
