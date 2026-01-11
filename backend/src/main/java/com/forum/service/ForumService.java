package com.forum.service;

import com.forum.dto.AuthorDto;
import com.forum.dto.CommentResponse;
import com.forum.dto.PostResponse;
import com.forum.model.*;
import com.forum.model.university.UniversityCourse;
import com.forum.repository.*;
import com.forum.repository.university.UniversityCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    private UniversityCourseRepository uniCourseRepo;

    @Autowired
    private NotificationService notificationService;

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

    @Transactional
    public List<Forum> getForumsForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            return forumRepository.findAll();
        }
        
        if (user.getRole() == Role.PROFESSOR) {
            return forumRepository.findByProfessorId(user.getId());
        }
        
        // For Students:
        // 1. Get Enrolled Courses (Explicit enrollment only)
        List<UniversityCourse> relevantCourses = new ArrayList<>();
        if (user.getCourses() != null) {
            relevantCourses.addAll(user.getCourses());
        }
        
        // Remove duplicates
        relevantCourses = relevantCourses.stream().distinct().toList();
        
        List<Forum> mainForums = new ArrayList<>();
        if (!relevantCourses.isEmpty()) {
            mainForums = forumRepository.findByCourseInAndType(relevantCourses, Forum.ForumType.MAIN_COURSE);
        }
        
        // 2. Get Sub-forums for their Group
        // Logic: Return forums where course is in relevantCourses AND group matches user's group
        List<Forum> groupForums = new ArrayList<>();
        if (user.getGroupName() != null && !relevantCourses.isEmpty()) {
            groupForums = forumRepository.findByCourseInAndGroupNameAndType(relevantCourses, user.getGroupName(), Forum.ForumType.GROUP_SUBFORUM);
        }
        
        mainForums.addAll(groupForums);
        
        // 3. Add forums where user is explicitly allowed
        List<Forum> allowedForums = forumRepository.findAll().stream()
                .filter(f -> f.getAllowedUsers() != null && f.getAllowedUsers().contains(user))
                .toList();
        mainForums.addAll(allowedForums);

        return mainForums.stream().distinct().toList();
    }

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
    
    public List<PostResponse> getPosts(Long forumId) {
        if (forumId == null) throw new IllegalArgumentException("Forum ID required");
        List<Post> posts = postRepository.findByForumIdOrderByIsPinnedDescScoreDescTimestampDesc(forumId);
        return posts.stream().map(this::mapToPostResponse).collect(Collectors.toList());
    }
    
    public PostResponse getPost(Long postId) {
        if (postId == null) throw new IllegalArgumentException("Post ID required");
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToPostResponse(post);
    }
    
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

        // Notification Logic: Notify students if an official (Professor/Admin) responds
        if (user.getRole() == Role.PROFESSOR || user.getRole() == Role.ADMIN) {
            // 1. Notify Post Author if they are a student
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

            // 2. Notify Parent Comment Author if they are a student
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

    public void votePost(Long postId, int value, String userEmail) {
        if (value != 1 && value != -1) throw new IllegalArgumentException("Vote value must be 1 or -1");
        
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Check if vote exists
        Vote existingVote = voteRepository.findByUserIdAndPostId(user.getId(), postId).orElse(null);
        
        if (existingVote != null) {
            // If clicking same vote, remove it (toggle off)
            if (existingVote.getValue() == value) {
                post.setScore(post.getScore() - value);
                voteRepository.delete(existingVote);
            } else {
                // Changing vote (e.g. +1 to -1)
                // Remove old value (-1) and add new value (-1) -> net -2 change
                // or remove old value (+1) and add new value (+1)
                // Actually: old was +1, new is -1. Score should decrease by 2.
                // old was -1, new is +1. Score should increase by 2.
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
    
    public void voteComment(Long commentId, int value, String userEmail) {
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

    private PostResponse mapToPostResponse(Post post) {
        List<CommentResponse> commentResponses = new ArrayList<>();
        if (post.getComments() != null) {
            // Only fetch top-level comments (parent == null)
            commentResponses = post.getComments().stream()
                    .filter(c -> c.getParent() == null)
                    // Sort: Pinned first, then Score (highest), then Newest
                    // BUT user requirement: "popular responses raising to the surface (but not above the professor direct response)"
                    // AND "replying to a comment the order should be keep exactly how the messages come in"
                    
                    // Sorting logic for Top-Level Comments:
                    // 1. Pinned (Professor)
                    // 2. Score (Desc)
                    // 3. Timestamp (Desc) - usually new popular ones first or old ones? Reddit uses "Best". Let's stick to Score Desc.
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
                post.isPinned(),
                post.getScore(),
                commentResponses
        );
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        List<CommentResponse> replies = new ArrayList<>();
        if (comment.getReplies() != null) {
            replies = comment.getReplies().stream()
                // Nested comments: "order should be keep exactly how the messages come in" -> Chronological (Oldest first)
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
            // Students: Nickname only, no email
            dto.setDisplayName(user.getNickname());
            dto.setEmail(null); 
        } else if (user.getRole() == Role.ADMIN) {
            // Admin: Full Name, NO Email
            dto.setDisplayName(user.getFirstName() + " " + user.getLastName());
            dto.setEmail(null);
        } else {
            // Professors: Full Name + Email
            dto.setDisplayName(user.getFirstName() + " " + user.getLastName());
            dto.setEmail(user.getEmail());
        }
        return dto;
    }
}
