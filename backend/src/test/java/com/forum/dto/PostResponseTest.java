package com.forum.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostResponseTest {

    @Test
    void testNoArgsConstructor() {
        PostResponse response = new PostResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getComments());
    }

    @Test
    void testAllArgsConstructor() {
        AuthorDto author = new AuthorDto();
        author.setId(2L);
        LocalDateTime now = LocalDateTime.now();
        List<CommentResponse> comments = new ArrayList<>();

        PostResponse response = new PostResponse(200L, "Title", "Content", author, 5L, now, true, 20, comments);

        assertEquals(200L, response.getId());
        assertEquals("Title", response.getTitle());
        assertEquals("Content", response.getContent());
        assertEquals(author, response.getAuthor());
        assertEquals(5L, response.getForumId());
        assertEquals(now, response.getTimestamp());
        assertTrue(response.isPinned());
        assertEquals(20, response.getScore());
        assertEquals(comments, response.getComments());
    }

    @Test
    void testSettersAndGetters() {
        PostResponse response = new PostResponse();
        response.setTitle("Updated Title");
        response.setPinned(false);
        
        assertEquals("Updated Title", response.getTitle());
        assertFalse(response.isPinned());
    }
}
