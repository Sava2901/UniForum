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

/**
 * Controller for managing forums, posts, and comments.
 */
@RestController
@RequestMapping("/api/forums")
public class ForumController {
    @Autowired
    private ForumService forumService;

    /**
     * Retrieves forums available to the authenticated user.
     * @param authentication The current user's authentication.
     * @return List of accessible forums.
     */
    @GetMapping
    public ResponseEntity<List<Forum>> getForums(Authentication authentication) {
        return ResponseEntity.ok(forumService.getForumsForUser(authentication.getName()));
    }
    
    /**
     * Retrieves posts for a specific forum.
     * @param forumId The ID of the forum.
     * @return List of posts in the forum.
     */
    @GetMapping("/{forumId}/posts")
    public ResponseEntity<List<PostResponse>> getPosts(@PathVariable Long forumId) {
        return ResponseEntity.ok(forumService.getPosts(forumId));
    }
    
    /**
     * Creates a new post in a forum.
     * @param forumId The ID of the forum.
     * @param request The post creation request.
     * @param authentication The current user's authentication.
     * @return The created post.
     */
    @PostMapping("/{forumId}/posts")
    public ResponseEntity<PostResponse> createPost(@PathVariable Long forumId, @RequestBody PostRequest request, Authentication authentication) {
        return ResponseEntity.ok(forumService.createPost(forumId, request.getTitle(), request.getContent(), authentication.getName()));
    }
    
    /**
     * Retrieves a specific post by ID.
     * @param postId The ID of the post.
     * @return The post details.
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(forumService.getPost(postId));
    }
    
    /**
     * Adds a comment to a post.
     * @param postId The ID of the post.
     * @param payload Map containing the comment content and optional parentId.
     * @param authentication The current user's authentication.
     * @return The created comment.
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId, @RequestBody Map<String, Object> payload, Authentication authentication) {
        String content = (String) payload.get("content");
        Long parentId = payload.containsKey("parentId") && payload.get("parentId") != null 
                        ? Long.valueOf(payload.get("parentId").toString()) 
                        : null;
        return ResponseEntity.ok(forumService.addComment(postId, content, authentication.getName(), parentId));
    }

    /**
     * Votes on a post (upvote/downvote).
     * @param postId The ID of the post.
     * @param value The vote value (1 or -1).
     * @param authentication The current user's authentication.
     * @return A success message.
     */
    @PostMapping("/posts/{postId}/vote")
    public ResponseEntity<?> votePost(@PathVariable Long postId, @RequestParam int value, Authentication authentication) {
        forumService.votePost(postId, value, authentication.getName());
        return ResponseEntity.ok("Voted");
    }

    /**
     * Votes on a comment.
     * @param commentId The ID of the comment.
     * @param value The vote value (1 or -1).
     * @param authentication The current user's authentication.
     * @return A success message.
     */
    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<?> voteComment(@PathVariable Long commentId, @RequestParam int value, Authentication authentication) {
        forumService.voteComment(commentId, value, authentication.getName());
        return ResponseEntity.ok("Voted");
    }
}
