package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.PostLikeDto;
import org.example.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/resq/likes")
@RequiredArgsConstructor
public class PostLikeController {
    private final PostLikeService likeService;

    @PostMapping("")
    public ResponseEntity<String> likePost(@RequestBody PostLikeDto postLikeDto) {
        likeService.likePost(postLikeDto.getPostId(), postLikeDto.getUserId());
        return ResponseEntity.ok("Post liked");
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @RequestParam Long userId) {
        likeService.unlikePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikesCount(postId));
    }
}
