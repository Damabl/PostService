package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.entity.PostComment;
import org.example.service.PostCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("resq/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService commentService;
    @PostMapping("/{postId}")
    public ResponseEntity<Void> addComment(@PathVariable Long postId,
                                           @RequestParam Long userId,
                                           @RequestParam String content,
                                           @RequestParam(required = false) Long parentId) {
        commentService.addComment(postId, userId, content, parentId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{postId}")
    public ResponseEntity<List<PostComment>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }
    @GetMapping("/replies/{commentId}")
    public ResponseEntity<List<PostComment>> getReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }
}
