package com.forum.service;

import com.forum.dto.AuthorDto;
import com.forum.dto.CommentResponse;
import com.forum.dto.PostResponse;
import com.forum.model.*;
import com.forum.model.university.UniversityCourse;
import com.forum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service for forum business logic, including posts, comments, and votes.
 */
@Service
public class ForumService {
    @Autowired
    private ForumRepository forumRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Creates a group subforum if it doesn't exist for a given course and group.
     */
    public void createGroupSubforumIfMissing(UniversityCourse course, String groupName) {
        if (course == null || groupName == null) return;
        
        if (forumRepository.findByCourseAndGroupNameAndType(course, groupName, Forum.ForumType.GROUP_SUBFORUM).isEmpty()) {
            Forum subForum = new Forum();
            subForum.setCourse(course);
            subForum.setGroupName(groupName);
            subForum.setType(Forum.ForumType.GROUP_SUBFORUM);
            forumRepository.save(subForum);
        }
    }

    /**
     * Retrieves relevant forums based on user role and enrollment.
     */
    @Transactional
    public List<Forum> getForumsForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            return forumRepository.findAll();
        }
        
        if (user.getRole() == Role.PROFESSOR) {
            return forumRepository.findByProfessorId(user.getId());
        }
        
        // Students see forums for their courses
        List<UniversityCourse> relevantCourses = new ArrayList<>();
        if (user.getCourses() != null) {
            relevantCourses.addAll(user.getCourses());
        }
        
        relevantCourses = relevantCourses.stream().distinct().toList();
        
        List<Forum> mainForums = new ArrayList<>();
        if (!relevantCourses.isEmpty()) {
            mainForums = forumRepository.findByCourseInAndType(relevantCourses, Forum.ForumType.MAIN_COURSE);
        }
        
        // Add group subforums
        List<Forum> groupForums = new ArrayList<>();
        if (user.getGroupName() != null && !relevantCourses.isEmpty()) {
            groupForums = forumRepository.findByCourseInAndGroupNameAndType(relevantCourses, user.getGroupName(), Forum.ForumType.GROUP_SUBFORUM);
        }
        
        mainForums.addAll(groupForums);
        
        // Add manually allowed forums
        List<Forum> allowedForums = forumRepository.findAll().stream()
                .filter(f -> f.getAllowedUsers() != null && f.getAllowedUsers().contains(user))
                .toList();
        mainForums.addAll(allowedForums);

        return mainForums.stream().distinct().toList();
    }

    /**
     * Creates and saves a new post.
     */
    public PostResponse createPost(Long forumId, String title, String content, String userEmail) {
        if (forumId == null) throw new IllegalArgumentException("Forum ID required");
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Forum forum = forumRepository.findById(forumId).orElseThrow(() -> new RuntimeException("Forum not found"));
        
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(user);
        post.setForum(forum);
        
        if (user.getRole() == Role.PROFESSOR) {
            post.setPinned(true); // Auto-pin professor posts
        }
        
        Post savedPost = postRepository.save(post);
        return mapToPostResponse(savedPost);
    }
    
    /**
     * Retrieves posts for a forum, sorted by pin status and score.
     */
    public List<PostResponse> getPosts(Long forumId) {
        if (forumId == null) throw new IllegalArgumentException("Forum ID required");
        List<Post> posts = postRepository.findByForumIdOrderByIsPinnedDescScoreDescTimestampDesc(forumId);
        return posts.stream().map(this::mapToPostResponse).collect(Collectors.toList());
    }
    
    /**
     * Retrieves a single post.
     */
    public PostResponse getPost(Long postId) {
        if (postId == null) throw new IllegalArgumentException("Post ID required");
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToPostResponse(post);
    }
    
    /**
     * Adds a comment to a post, handling notifications.
     */
    public CommentResponse addComment(Long postId, String content, String userEmail, Long parentId) {
        if (postId == null) throw new IllegalArgumentException("Post ID required");
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(user);
        comment.setPost(post);
        
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parent);
        }

        if (user.getRole() == Role.PROFESSOR) {
            comment.setPinned(true);
        }
        
        Comment savedComment = commentRepository.save(comment);

        // Notify students of official responses
        if (user.getRole() == Role.PROFESSOR || user.getRole() == Role.ADMIN) {
            // Notify post author if they are a student
            User postAuthor = post.getAuthor();
            if (postAuthor.getRole() == Role.STUDENT && !postAuthor.getId().equals(user.getId())) {
                 boolean isDirectReplyToPostAuthor = (comment.getParent() != null && comment.getParent().getAuthor().getId().equals(postAuthor.getId()));
                 
                 if (comment.getParent() == null || !isDirectReplyToPostAuthor) {
                      notificationService.createNotification(
                          postAuthor,
                          "An official commented on your post: " + post.getTitle(),
                          "OFFICIAL_POST_COMMENT",
                          post.getId()
                      );
                 }
            }

            // Notify parent comment author if they are a student
            if (comment.getParent() != null) {
                User parentAuthor = comment.getParent().getAuthor();
                if (parentAuthor.getRole() == Role.STUDENT && !parentAuthor.getId().equals(user.getId())) {
                     notificationService.createNotification(
                         parentAuthor,
                         "An official replied to your comment.",
                         "OFFICIAL_COMMENT_REPLY",
                         post.getId()
                     );
                }
            }
        }

        return mapToCommentResponse(savedComment);
    }

    @Autowired
    private VoteRepository voteRepository;

    /**
     * Handles voting on a post.
     */
    public void votePost(Long postId, int value, String userEmail) {
        if (postId == null) throw new IllegalArgumentException("Post ID required");
        if (value != 1 && value != -1) throw new IllegalArgumentException("Vote value must be 1 or -1");
        
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Check existing vote
        Vote existingVote = voteRepository.findByUserIdAndPostId(user.getId(), postId).orElse(null);
        
        if (existingVote != null) {
            // Undo vote if same value, else change vote
            if (existingVote.getValue() == value) {
                post.setScore(post.getScore() - value);
                voteRepository.delete(existingVote);
            } else {
                // Change vote: remove old value, add new value (difference is 2*value or similar logic)
                // Actually: score - old + new
                post.setScore(post.getScore() - existingVote.getValue() + value);
                existingVote.setValue(value);
                voteRepository.save(existingVote);
            }
        } else {
            // New vote
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setPost(post);
            vote.setValue(value);
            voteRepository.save(vote);
            post.setScore(post.getScore() + value);
        }
        postRepository.save(post);
    }
    
    /**
     * Handles voting on a comment.
     */
    public void voteComment(Long commentId, int value, String userEmail) {
        if (commentId == null) throw new IllegalArgumentException("Comment ID required");
        if (value != 1 && value != -1) throw new IllegalArgumentException("Vote value must be 1 or -1");
        
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        
        Vote existingVote = voteRepository.findByUserIdAndCommentId(user.getId(), commentId).orElse(null);
        
        if (existingVote != null) {
            if (existingVote.getValue() == value) {
                comment.setScore(comment.getScore() - value);
                voteRepository.delete(existingVote);
            } else {
                comment.setScore(comment.getScore() - existingVote.getValue() + value);
                existingVote.setValue(value);
                voteRepository.save(existingVote);
            }
        } else {
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setComment(comment);
            vote.setValue(value);
            voteRepository.save(vote);
            comment.setScore(comment.getScore() + value);
        }
        commentRepository.save(comment);
    }

    /**
     * Deletes a post. Accessible by admins or the post author.
     */
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        if (postId == null) throw new IllegalArgumentException("Post ID required");
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (user.getRole() != Role.ADMIN && !post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Only admins or the author can delete this post");
        }
        if (post != null) {
            postRepository.delete(post);
        }
    }

    /**
     * Deletes a comment. Accessible by admins or the comment author.
     */
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        if (commentId == null) throw new IllegalArgumentException("Comment ID required");
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (user.getRole() != Role.ADMIN && !comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Only admins or the author can delete this comment");
        }
        if (comment != null) {
            commentRepository.delete(comment);
        }
    }

    /**
     * Updates a post. Accessible only by the post author.
     */
    @Transactional
    public PostResponse updatePost(Long postId, String newContent, String userEmail) {
        if (postId == null) throw new IllegalArgumentException("Post ID required");
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Only the author can edit this post");
        }
        
        post.setContent(newContent);
        post.setEditedAt(java.time.LocalDateTime.now());
        Post updatedPost = postRepository.save(post);
        return mapToPostResponse(updatedPost);
    }

    /**
     * Updates a comment. Accessible only by the comment author.
     */
    @Transactional
    public CommentResponse updateComment(Long commentId, String newContent, String userEmail) {
        if (commentId == null) throw new IllegalArgumentException("Comment ID required");
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Only the author can edit this comment");
        }
        
        comment.setContent(newContent);
        comment.setEditedAt(java.time.LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentResponse(updatedComment);
    }

    private PostResponse mapToPostResponse(Post post) {
        List<CommentResponse> commentResponses = new ArrayList<>();
        if (post.getComments() != null) {
            // Sort comments: pinned first, then by score, then by date
            commentResponses = post.getComments().stream()
                    .filter(c -> c.getParent() == null) // Top level comments
                    .sorted((c1, c2) -> {
                        if (c1.isPinned() != c2.isPinned()) return c1.isPinned() ? -1 : 1;
                        if (c1.getScore() != c2.getScore()) return c2.getScore() - c1.getScore();
                        return c2.getTimestamp().compareTo(c1.getTimestamp());
                    })
                    .map(this::mapToCommentResponse)
                    .collect(Collectors.toList());
        }

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                mapToAuthorDto(post.getAuthor()),
                post.getForum().getId(),
                post.getTimestamp(),
                post.getEditedAt(),
                post.isPinned(),
                post.getScore(),
                commentResponses
        );
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        List<CommentResponse> replies = new ArrayList<>();
        if (comment.getReplies() != null) {
            replies = comment.getReplies().stream()
                // Replies sorted by date usually
                .sorted((c1, c2) -> c1.getTimestamp().compareTo(c2.getTimestamp()))
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
        }

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                mapToAuthorDto(comment.getAuthor()),
                comment.getPost().getId(),
                comment.getTimestamp(),
                comment.getEditedAt(),
                comment.getScore(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                replies
        );
    }

    private AuthorDto mapToAuthorDto(User user) {
        AuthorDto dto = new AuthorDto();
        dto.setId(user.getId());
        dto.setRole(user.getRole());

        if (user.getRole() == Role.STUDENT) {
            // Anonymize students mostly, or show nickname
            dto.setDisplayName(user.getNickname());
            dto.setEmail(null); 
        } else if (user.getRole() == Role.ADMIN) {
            // Admins show name
            dto.setDisplayName(user.getFirstName() + " " + user.getLastName());
            dto.setEmail(null);
        } else {
            // Professors show name and email
            dto.setDisplayName(user.getFirstName() + " " + user.getLastName());
            dto.setEmail(user.getEmail());
        }
        return dto;
    }
}
