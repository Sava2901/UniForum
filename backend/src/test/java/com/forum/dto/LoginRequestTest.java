package com.forum.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void testNoArgsConstructor() {
        LoginRequest request = new LoginRequest();
        assertNotNull(request);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("securePass");

        assertEquals("user@example.com", request.getEmail());
        assertEquals("securePass", request.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        LoginRequest r1 = new LoginRequest();
        r1.setEmail("a");
        r1.setPassword("b");
        
        LoginRequest r2 = new LoginRequest();
        r2.setEmail("a");
        r2.setPassword("b");
        
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
