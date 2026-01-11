package com.forum.dto;

import com.forum.model.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    @Test
    void testNoArgsConstructor() {
        RegisterRequest request = new RegisterRequest();
        assertNotNull(request);
        assertNull(request.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("First");
        request.setLastName("Last");
        request.setEmail("email@test.com");
        request.setPassword("pass");
        request.setConfirmPassword("pass");
        request.setNickname("nick");
        request.setRole(Role.STUDENT);
        request.setGroupId(10L);

        assertEquals("First", request.getFirstName());
        assertEquals("Last", request.getLastName());
        assertEquals("email@test.com", request.getEmail());
        assertEquals("pass", request.getPassword());
        assertEquals("pass", request.getConfirmPassword());
        assertEquals("nick", request.getNickname());
        assertEquals(Role.STUDENT, request.getRole());
        assertEquals(10L, request.getGroupId());
    }

    @Test
    void testEquality() {
        RegisterRequest r1 = new RegisterRequest();
        r1.setEmail("e");
        RegisterRequest r2 = new RegisterRequest();
        r2.setEmail("e");
        
        assertEquals(r1, r2);
    }
}
