package org.example.service;

import org.example.client.UserServiceClient;
import org.example.dto.PostPreviewDto;
import org.example.dto.UserInfoDto;
import org.example.dto.mapper.PostMapper;
import org.example.model.entity.Image;
import org.example.model.entity.Post;
import org.example.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private PostRedisService postRedisService;


    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private PostCommentService postCommentService;

    @Mock
    private PostMapper postMapper;
    @InjectMocks
    private PostService postService;
    @Test
    void addPost() {

    }

    @Test
    void searchPosts() {
    }

    @Test
    void updateUserCategories() {
    }

    @Test
    //Should return PostPreviewDtos when User has Categories and Posts
    void getPostsByUserId() {
        // given
        Long userId = 1L;
        List<String> categoryIds = List.of("tech", "java");

        Post post = new Post();
        post.setId(100L);
        post.setUserId(userId);
        post.setTitle("Test Post");
        post.setContent("This is a test post");
        post.setCreatedAt(LocalDateTime.of(2024, 4, 1, 10, 30));

        List<Post> postList = List.of(post);
        Page<Post> postPage = new PageImpl<>(postList);

        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setUsername("john_doe");
        userInfo.setAvatarId(777L);

        long likeCount = 42L;
        long commentCount = 7L;

        PostPreviewDto expectedDto = new PostPreviewDto(
                100L,
                userId,
                "Test Post",
                "This is a test post",
                "john_doe",
                777L,
                likeCount,
                commentCount,
                LocalDateTime.of(2024, 4, 1, 10, 30)
        );

        // when
        when(userServiceClient.getUserCategories(userId)).thenReturn(categoryIds);
        when(postRepository.findByCategories(eq(categoryIds), any(Pageable.class))).thenReturn(postPage);
        when(userServiceClient.getUserInfo(userId)).thenReturn(userInfo);
        when(postLikeService.getLikesCount(100L)).thenReturn(likeCount);
        when(postCommentService.getCommentsCount(100L)).thenReturn(commentCount);
        when(postMapper.toDto(
                post,
                "john_doe",
                777L,
                likeCount,
                commentCount
        )).thenReturn(expectedDto);

        int limit = 10;
        Long afterPostId = null;

        List<PostPreviewDto> result = postService.getPostsByUserId(userId, afterPostId, limit);

        assertEquals(1, result.size());
        PostPreviewDto actual = result.get(0);
        assertEquals(expectedDto.getPostId(), actual.getPostId());
        assertEquals(expectedDto.getUserId(), actual.getUserId());
        assertEquals(expectedDto.getTitle(), actual.getTitle());
        assertEquals(expectedDto.getContent(), actual.getContent());
        assertEquals(expectedDto.getUsername(), actual.getUsername());
        assertEquals(expectedDto.getAvatarId(), actual.getAvatarId());
        assertEquals(expectedDto.getLikeCount(), actual.getLikeCount());
        assertEquals(expectedDto.getCommentCount(), actual.getCommentCount());
        assertEquals(expectedDto.getCreatedAt(), actual.getCreatedAt());
    }


//    @Test
//    void getPostsByUserId_shouldReturnEmptyList_whenNoCategories() {
//        // given
//        Long userId = 1L;
//
//        when(userServiceClient.getUserCategories(userId)).thenReturn(List.of());
//
//        // then
//        List<PostPreviewDto> result = postService.getPostsByUserId(userId);
//        assertTrue(result.isEmpty());
//        verifyNoInteractions(postRepository, postLikeService, postCommentService, postMapper);
//    }

    @Test
    void testReplaceNameImages_withImages() {
        List<Image> images = List.of(new Image(1L,"img1.jpg","jpg"), new Image(2L,"img2.png","png"));
        String content = "Here is image {image:0} and another {image:1}";
        String expected = "Here is image <img src=\"http://localhost:8081/images/img1.jpg\" alt=\"image\"/> and another <img src=\"http://localhost:8081/images/img2.png\" alt=\"image\"/>";
        ReflectionTestUtils.setField(postService, "urlImage", "http://localhost:8081/images/");
        String result = postService.replaceNameImages(content, images);
        assertEquals(expected, result);
    }

    @Test
    void testReplaceNameImages_emptyImages() {
        String content = "No images here";
        String result = postService.replaceNameImages(content, new ArrayList<>());
        assertEquals("No images here", result);
    }

    @Test
    void testReplaceNameImages_nullImages() {
        String content = "No images here";
        String result = postService.replaceNameImages(content, null);
        assertEquals("No images here", result);
    }

    @Test
    void testCacheTop100Posts() {
        List<Long> postIds = List.of(1L, 2L, 3L);

        Post post1 = new Post(1L, 2L,"Post 1");
        Post post2 = new Post(2L, 3L,"Post 2");
        Post post3 = new Post(3L, 4L,"Post 3");

        when(postRepository.findAllById(postIds)).thenReturn(List.of(post1, post2, post3));

        String result = postService.cacheTop100Posts(postIds);

        verify(postRedisService).clearTopPosts();
        verify(postRedisService).savePost(post1, 100);
        verify(postRedisService).savePost(post2, 99);
        verify(postRedisService).savePost(post3, 98);

        assertEquals("Топ-100 постов обновлены в Redis.", result);
    }

    @Test
    void deletePost() {
    }
}