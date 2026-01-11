package com.forum.dto;

import com.forum.model.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtResponseTest {

    @Test
    void testConstructor() {
        String token = "jwt-token-123";
        Long id = 100L;
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        Role role = Role.STUDENT;
        String nickname = "tester";
        String groupName = "GroupA";
        Integer studyYear = 2;
        Integer semester = 1;

        JwtResponse response = new JwtResponse(token, id, email, firstName, lastName, role, nickname, groupName, studyYear, semester);

        assertEquals(token, response.getToken());
        assertEquals(id, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(firstName, response.getFirstName());
        assertEquals(lastName, response.getLastName());
        assertEquals(role, response.getRole());
        assertEquals(nickname, response.getNickname());
        assertEquals(groupName, response.getGroupName());
        assertEquals(studyYear, response.getStudyYear());
        assertEquals(semester, response.getSemester());
    }

    @Test
    void testSetters() {
        JwtResponse response = new JwtResponse(null, null, null, null, null, null, null, null, null, null);
        response.setToken("newToken");
        response.setId(5L);
        response.setEmail("new@email.com");

        assertEquals("newToken", response.getToken());
        assertEquals(5L, response.getId());
        assertEquals("new@email.com", response.getEmail());
    }

    @Test
    void testEquality() {
        JwtResponse r1 = new JwtResponse("token", 1L, "e", "f", "l", Role.ADMIN, "n", "g", 1, 1);
        JwtResponse r2 = new JwtResponse("token", 1L, "e", "f", "l", Role.ADMIN, "n", "g", 1, 1);
        
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
