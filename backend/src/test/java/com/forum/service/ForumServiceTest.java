package com.forum.service;

import com.forum.model.*;
import com.forum.repository.CommentRepository;
import com.forum.repository.PostRepository;
import com.forum.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class ForumServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ForumService forumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddComment_OfficialRepliesToStudentPost_ShouldNotify() {
        // Arrange
        Long postId = 1L;
        String officialEmail = "prof@univ.edu";
        
        User studentAuthor = new User();
        studentAuthor.setId(2L);
        studentAuthor.setRole(Role.STUDENT);
        
        User professorUser = new User();
        professorUser.setId(3L);
        professorUser.setRole(Role.PROFESSOR);
        professorUser.setEmail(officialEmail);
        
        Post post = new Post();
        post.setId(postId);
        post.setAuthor(studentAuthor);
        post.setTitle("Student Question");
        
        when(userRepository.findByEmail(officialEmail)).thenReturn(Optional.of(professorUser));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        // Act
        forumService.addComment(postId, "Here is the answer", officialEmail, null);

        // Assert
        verify(notificationService, times(1)).createNotification(
                eq(studentAuthor),
                contains("An official commented on your post"),
                eq("OFFICIAL_POST_COMMENT"),
                eq(postId)
        );
    }

    @Test
    void testAddComment_StudentRepliesToStudentPost_ShouldNotNotify() {
        // Arrange
        Long postId = 1L;
        String studentEmail = "student2@univ.edu";
        
        User studentAuthor = new User();
        studentAuthor.setId(2L);
        studentAuthor.setRole(Role.STUDENT);
        
        User otherStudent = new User();
        otherStudent.setId(4L);
        otherStudent.setRole(Role.STUDENT);
        otherStudent.setEmail(studentEmail);
        
        Post post = new Post();
        post.setId(postId);
        post.setAuthor(studentAuthor);
        
        when(userRepository.findByEmail(studentEmail)).thenReturn(Optional.of(otherStudent));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(11L);
            return c;
        });

        // Act
        forumService.addComment(postId, "Me too", studentEmail, null);

        // Assert
        verify(notificationService, never()).createNotification(any(), any(), any(), any());
    }

    @Test
    void testAddComment_OfficialRepliesToStudentComment_ShouldNotifyCommentAuthor() {
        // Arrange
        Long postId = 1L;
        Long parentCommentId = 5L;
        String officialEmail = "admin@univ.edu";
        
        User studentPostAuthor = new User();
        studentPostAuthor.setId(2L);
        studentPostAuthor.setRole(Role.STUDENT);
        
        User studentCommentAuthor = new User();
        studentCommentAuthor.setId(6L);
        studentCommentAuthor.setRole(Role.STUDENT);
        
        User adminUser = new User();
        adminUser.setId(7L);
        adminUser.setRole(Role.ADMIN);
        adminUser.setEmail(officialEmail);
        
        Post post = new Post();
        post.setId(postId);
        post.setAuthor(studentPostAuthor);
        
        Comment parentComment = new Comment();
        parentComment.setId(parentCommentId);
        parentComment.setAuthor(studentCommentAuthor);
        parentComment.setPost(post);
        
        when(userRepository.findByEmail(officialEmail)).thenReturn(Optional.of(adminUser));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(12L);
            c.setParent(parentComment);
            return c;
        });

        // Act
        forumService.addComment(postId, "Clarification", officialEmail, parentCommentId);

        // Assert
        // Should notify comment author
        verify(notificationService, times(1)).createNotification(
                eq(studentCommentAuthor),
                contains("An official replied to your comment"),
                eq("OFFICIAL_COMMENT_REPLY"),
                eq(postId)
        );
        
        // Should NOT notify post author again for "official commented on your post" if logic excludes direct replies?
        // My implementation says: "if (comment.getParent() == null || !isDirectReplyToPostAuthor)"
        // Here comment.getParent() != null.
        // isDirectReplyToPostAuthor = parentAuthor (studentCommentAuthor) == postAuthor (studentPostAuthor)?
        // IDs are 6L vs 2L. So false.
        // So it MIGHT notify post author too.
        // Let's check the code logic again:
        // if (comment.getParent() == null || !isDirectReplyToPostAuthor)
        // isDirectReplyToPostAuthor is false.
        // So !false is true.
        // So it notifies post author too.
        
        verify(notificationService, times(1)).createNotification(
                eq(studentPostAuthor),
                contains("An official commented on your post"),
                eq("OFFICIAL_POST_COMMENT"),
                eq(postId)
        );
    }
}
