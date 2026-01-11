package com.forum.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostRequestTest {

    @Test
    void testNoArgsConstructor() {
        PostRequest request = new PostRequest();
        assertNotNull(request);
        assertNull(request.getTitle());
        assertNull(request.getContent());
    }

    @Test
    void testSettersAndGetters() {
        PostRequest request = new PostRequest();
        request.setTitle("My Title");
        request.setContent("My Content");

        assertEquals("My Title", request.getTitle());
        assertEquals("My Content", request.getContent());
    }

    @Test
    void testToString() {
        PostRequest request = new PostRequest();
        request.setTitle("T");
        request.setContent("C");
        
        String str = request.toString();
        assertTrue(str.contains("T"));
        assertTrue(str.contains("C"));
    }
}
