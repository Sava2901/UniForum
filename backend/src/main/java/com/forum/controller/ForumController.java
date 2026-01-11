package com.forum.controller;

import com.forum.model.Forum;
import com.forum.dto.PostRequest;
import com.forum.dto.PostResponse;
import com.forum.dto.CommentResponse;
import com.forum.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forums")
// @CrossOrigin(origins = "http://localhost:5173") // Handled globally
public class ForumController {
    @Autowired
    private ForumService forumService;

    @GetMapping
    public ResponseEntity<List<Forum>> getForums(Authentication authentication) {
        return ResponseEntity.ok(forumService.getForumsForUser(authentication.getName()));
    }
    
    @GetMapping("/{forumId}/posts")
    public ResponseEntity<List<PostResponse>> getPosts(@PathVariable Long forumId) {
        return ResponseEntity.ok(forumService.getPosts(forumId));
    }
    
    @PostMapping("/{forumId}/posts")
    public ResponseEntity<PostResponse> createPost(@PathVariable Long forumId, @RequestBody PostRequest request, Authentication authentication) {
        return ResponseEntity.ok(forumService.createPost(forumId, request.getTitle(), request.getContent(), authentication.getName()));
    }
    
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(forumService.getPost(postId));
    }
    
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId, @RequestBody Map<String, Object> payload, Authentication authentication) {
        String content = (String) payload.get("content");
        Long parentId = payload.containsKey("parentId") && payload.get("parentId") != null 
                        ? Long.valueOf(payload.get("parentId").toString()) 
                        : null;
        return ResponseEntity.ok(forumService.addComment(postId, content, authentication.getName(), parentId));
    }

    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<?> votePost(@PathVariable Long postId, @RequestParam int value, Authentication authentication) {
        forumService.votePost(postId, value, authentication.getName());
        return ResponseEntity.ok("Voted");
    }

    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<?> voteComment(@PathVariable Long commentId, @RequestParam int value, Authentication authentication) {
        forumService.voteComment(commentId, value, authentication.getName());
        return ResponseEntity.ok("Voted");
    }
}
