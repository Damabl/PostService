package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.client.UserServiceClient;
import org.example.dto.PostDto;
import org.example.events.PostCreatedEvent;
import org.example.exception.exceptions.PostNotFoundException;
import org.example.model.entity.Image;
import org.example.model.entity.Post;
import org.example.properties.TopicProperties;
import org.example.repository.PostRepository;
import org.example.repository.PostSearchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    @Value("${image.url}")
    private String urlImage;
    private final PostRepository repository;
    private final ImageService imageService;
    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;
    private final TopicProperties topicProperties;
    private final UserServiceClient userServiceClient;
    private final PostSearchRepository postSearchRepository;
    public String addPost(PostDto postDto) {
        Post post = new Post();
        post.setUserId(postDto.getUserId());
        List<Long> imageIds = imageService.uploadMultiple(postDto.getFiles());
        List<Image> images = imageIds.stream()
                .map(imageService::getImageById)
                .toList();

        post.setImages(images);
        String content=replaceNameImages(postDto.getContent(),images);
        post.setContent(content);
        PostCreatedEvent event = new PostCreatedEvent(post.getId(), postDto.getUserId());
        kafkaTemplate.send(topicProperties.getCommentPost(), event);
        repository.save(post);
        return "Added";
    }
    public List<Post> searchPosts(String keyword) {
        Pageable pageable = PageRequest.of(0, 20); // Первая страница, 20 постов
        Page<Post> postPage= postSearchRepository.findByContentContaining(keyword,pageable);
        return postPage.getContent();
    }

    @Transactional
    public void updateUserCategories(Long postId, List<String> categoryIds) {
        Post post = repository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("User not found"));
        post.setCategories(categoryIds);
        repository.save(post);
    }

    public List<Post> getPostsByUserId(Long userId) {
        List<String> categoryIds = userServiceClient.getUserCategories(userId);
        if (categoryIds.isEmpty()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(0, 20); // Первая страница, 20 постов
        Page<Post> postsPage = repository.findByCategories(categoryIds, pageable);

        return postsPage.getContent();
    }
    public String replaceNameImages(String content, List<Image> images) {
        if (images == null || images.isEmpty()) {
            return content;
        }

        for (int i = 0; i < images.size(); i++) {
            String imageTag = "{image:" + i + "}";
            if (content.contains(imageTag)) {
                content = content.replace(imageTag, "<img src=\""+ urlImage + images.get(i).getName() + "\" alt=\"image\"/>");
            }
        }

        return content;
    }

    public String deletePost(Long id) throws IOException {
        Post post = repository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));

        List<Image> images = post.getImages();
        if (images != null) {
            for (Image image : images) {
                imageService.deleteImage(image.getName());
            }
        }
        repository.deleteById(id);
        return "Deleted";
    }

}
