package org.example.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PostPreviewDto;
import org.example.dto.UserInfoDto;
import org.example.dto.mapper.PostMapper;
import org.springframework.data.domain.Sort;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostCommentService postCommentService;
    @Value("${image.url}")
    private String urlImage;
    private final PostRepository postRepository;
    private final ImageService imageService;
    private final PostRedisService postRedisService;
    private final KafkaTemplate<String, PostCreatedEvent> kafkaTemplate;
    private final TopicProperties topicProperties;
    private final UserServiceClient userServiceClient;
    private final PostLikeService postLikeService;
    private final PostMapper postMapper;

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
        String redisKey = "search:" + keyword.toLowerCase();
        List<Post> cachedPosts = postRedisService.getCachedSearchResults(redisKey);
        if (cachedPosts != null && !cachedPosts.isEmpty()) {
            return cachedPosts;
        }
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            Pageable pageable = PageRequest.of(0, 20);
            List<Post> posts = postRepository.findByContentContaining(keyword, pageable).getContent();
            postRedisService.cacheSearchResults(redisKey, posts);
            return posts;
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
    public List<PostPreviewDto> getPostsByUserId(Long userId, Long afterPostId, int limit) {
        DatabaseContextHolder.setDatabaseType(DatabaseType.SLAVE);
        try {
            List<String> categoryIds = userServiceClient.getUserCategories(userId);
            if (categoryIds.isEmpty()) return List.of();
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
            List<Post> posts;
            if (afterPostId == null) {
                posts = postRepository.findByCategories(categoryIds, pageable).getContent();
            } else {
                posts = postRepository.findByCategoriesAndIdLessThan(categoryIds, afterPostId, pageable).getContent();
            }
            List<PostPreviewDto> response = new ArrayList<>();
            for (Post post : posts) {
                UserInfoDto userInfo = userServiceClient.getUserInfo(post.getUserId());
                long likeCount = postLikeService.getLikesCount(post.getId());
                long commentCount = postCommentService.getCommentsCount(post.getId());
                response.add(postMapper.toDto(
                        post,
                        userInfo.getUsername(),
                        userInfo.getAvatarId(),
                        likeCount,
                        commentCount
                ));
            }
            return response;
        } finally {
            DatabaseContextHolder.clear();
        }
    }


    public String getContentById(Long postId) {
        String content=postRepository.findById(postId).get().getContent();
        return content;
    }
    public String replaceNameImages(String content, List<Image> images) {
        if (images == null || images.isEmpty()) return content;
        for (int i = 0; i < images.size(); i++) {
            content = content.replace("{image:" + i + "}", "<img src=\"" + urlImage + images.get(i).getName() + "\" alt=\"image\"/>");
        }
        return content;
    }
    public String cacheTop100Posts(List<Long> postIds) {
        List<Post> posts = postRepository.findAllById(postIds);
        postRedisService.clearTopPosts();
        int score = 100;
        for (Post post : posts) {
            postRedisService.savePost(post, score--);
        }
        return "Топ-100 постов обновлены в Redis.";
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
