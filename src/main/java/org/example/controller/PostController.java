package org.example.controller;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.example.dto.PostDto;
import org.example.dto.PostPreviewDto;
import org.example.model.entity.Post;
import org.example.payload.ResponseMessage;
import org.example.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@Controller
@CrossOrigin("*")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<PostPreviewDto>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Long afterPostId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<PostPreviewDto> posts = postService.getPostsByUserId(userId, afterPostId, limit);
        return ResponseEntity.ok(posts);
    }
    @SneakyThrows
    @PostMapping("/post")
    public ResponseEntity<ResponseMessage> addPost(@ModelAttribute PostDto postDto) {
        postService.addPost(postDto);
        return ResponseEntity.ok(new ResponseMessage("Post added successfully"));
    }
    @PostMapping("/post/{keyword}/search")
    public ResponseEntity<List<Post>> searchPosts(@PathVariable String keyword) {
        return ResponseEntity.ok(postService.searchPosts(keyword));
    }

    @PostMapping("/{postId}/categories")
    public ResponseEntity<String> updateCategories(
            @PathVariable Long postId,
            @RequestBody List<String> categoryIds) {
        postService.updateUserCategories(postId, categoryIds);
        return ResponseEntity.ok("Categories updated successfully");
    }
    @GetMapping("/posts/{id}/content")
    public ResponseEntity<String> getPostContent(@PathVariable Long id) {
        String content=postService.getContentById(id);
        return ResponseEntity.ok(content);
    }
    @DeleteMapping("/post/{id}")
    @SneakyThrows
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        String result = postService.deletePost(id);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/top100")
    public ResponseEntity<String> addTop100Posts(@RequestBody List<Long> postIds) {
        String result = postService.cacheTop100Posts(postIds);
        return ResponseEntity.ok(result);
    }

}
