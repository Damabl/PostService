package org.example.controller;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PostDto;
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
@RequestMapping("/resq")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

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
