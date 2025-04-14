package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.PostLikeDto;
import org.example.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class PostLikeController {
    private final PostLikeService likeService;

    @PostMapping("/toggle")
    public ResponseEntity<Boolean> toggleLike(@RequestBody PostLikeDto postLikeDto) {
        boolean like=likeService.toggleLike(postLikeDto.getPostId(), postLikeDto.getUserId());
        return ResponseEntity.ok(like);
    }

    @GetMapping("/{postId}/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikesCount(postId));
    }
    @GetMapping("/{postId}/liked")
    public ResponseEntity<Boolean> isPostLiked(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        boolean liked = likeService.isPostLikedByUser(postId, userId);
        return ResponseEntity.ok(liked);
    }
}
