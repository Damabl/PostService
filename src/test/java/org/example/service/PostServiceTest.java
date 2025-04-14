package org.example.service;

import org.example.model.entity.Image;
import org.example.model.entity.Post;
import org.example.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private PostRedisService postRedisService;

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
    void getPostsByUserId() {
    }

    @Test
    void testReplaceNameImages_withImages() {
        List<Image> images = List.of(new Image(1L,"img1.jpg","jpg"), new Image(2L,"img2.png","png"));
        String content = "Here is image {image:0} and another {image:1}";
        String expected = "Here is image <img src=\"https://example.com/images/img1.jpg\" alt=\"image\"/> and another <img src=\"https://example.com/images/img2.png\" alt=\"image\"/>";

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
        MockitoAnnotations.openMocks(this);
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