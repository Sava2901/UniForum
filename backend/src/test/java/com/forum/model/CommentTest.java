package com.forum.model;

import com.forum.model.Comment;
import com.forum.model.User;
import com.forum.model.Post;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void testNoArgsConstructor() {
        Comment comment = new Comment();
        assertNotNull(comment);
        assertNull(comment.getId());
        assertNotNull(comment.getTimestamp()); // Default initialization
        assertFalse(comment.isPinned()); // Default value
        assertEquals(0, comment.getScore()); // Default value
    }

    @Test
    void testAllArgsConstructor() {
        User author = new User();
        author.setId(2L);
        Post post = new Post();
        post.setId(20L);
        Comment parent = new Comment();
        parent.setId(5L);
        List<Comment> replies = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment(50L, "Some content", author, post, now, true, parent, replies, 15);

        assertEquals(50L, comment.getId());
        assertEquals("Some content", comment.getContent());
        assertEquals(author, comment.getAuthor());
        assertEquals(post, comment.getPost());
        assertEquals(now, comment.getTimestamp());
        assertTrue(comment.isPinned());
        assertEquals(parent, comment.getParent());
        assertEquals(replies, comment.getReplies());
        assertEquals(15, comment.getScore());
    }

    @Test
    void testRepliesManagement() {
        Comment parent = new Comment();
        Comment reply1 = new Comment();
        reply1.setContent("Reply 1");
        
        List<Comment> replies = new ArrayList<>();
        replies.add(reply1);
        
        parent.setReplies(replies);
        
        assertEquals(1, parent.getReplies().size());
        assertEquals("Reply 1", parent.getReplies().get(0).getContent());
    }
}
