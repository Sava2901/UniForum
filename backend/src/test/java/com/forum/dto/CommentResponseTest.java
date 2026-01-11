package com.forum.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentResponseTest {

    @Test
    void testNoArgsConstructor() {
        CommentResponse response = new CommentResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getReplies());
    }

    @Test
    void testAllArgsConstructor() {
        AuthorDto author = new AuthorDto();
        author.setId(1L);
        LocalDateTime now = LocalDateTime.now();
        List<CommentResponse> replies = new ArrayList<>();
        
        CommentResponse response = new CommentResponse(100L, "Content", author, 50L, now, null, 5, 20L, replies);

        assertEquals(100L, response.getId());
        assertEquals("Content", response.getContent());
        assertEquals(author, response.getAuthor());
        assertEquals(50L, response.getPostId());
        assertEquals(now, response.getTimestamp());
        assertEquals(5, response.getScore());
        assertEquals(20L, response.getParentId());
        assertEquals(replies, response.getReplies());
    }

    @Test
    void testSettersAndGetters() {
        CommentResponse response = new CommentResponse();
        response.setContent("New Content");
        response.setScore(10);
        
        assertEquals("New Content", response.getContent());
        assertEquals(10, response.getScore());
    }
}
