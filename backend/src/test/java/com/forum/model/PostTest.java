package com.forum.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    @Test
    void testNoArgsConstructor() {
        Post post = new Post();
        assertNotNull(post);
        assertNull(post.getId());
        assertNotNull(post.getTimestamp()); // Should be initialized by default
        assertFalse(post.isPinned()); // Default value
        assertEquals(0, post.getScore()); // Default value
    }

    @Test
    void testAllArgsConstructor() {
        User author = new User();
        author.setId(1L);
        Forum forum = new Forum();
        forum.setId(10L);
        List<Comment> comments = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        Post post = new Post(100L, "Test Title", "Test Content", author, forum, now, true, 10, comments, null, new ArrayList<>());

        assertEquals(100L, post.getId());
        assertEquals("Test Title", post.getTitle());
        assertEquals("Test Content", post.getContent());
        assertEquals(author, post.getAuthor());
        assertEquals(forum, post.getForum());
        assertEquals(now, post.getTimestamp());
        assertTrue(post.isPinned());
        assertEquals(10, post.getScore());
        assertEquals(comments, post.getComments());
    }

    @Test
    void testSettersAndGetters() {
        Post post = new Post();
        post.setTitle("New Title");
        post.setContent("New Content");
        post.setScore(5);
        
        assertEquals("New Title", post.getTitle());
        assertEquals("New Content", post.getContent());
        assertEquals(5, post.getScore());
    }
}
