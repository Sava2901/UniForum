package com.forum.model;

import com.forum.model.university.UniversityCourse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class UserTest {

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getId());
        assertFalse(user.isVerified());
    }

    @Test
    void testAllArgsConstructor() {
        List<UniversityCourse> courses = new ArrayList<>();
        User user = new User(1L, "John", "Doe", "john@example.com", "password", Role.STUDENT, "johndoe", true, "GroupA", 2, 1, courses);
        
        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(Role.STUDENT, user.getRole());
        assertEquals("johndoe", user.getNickname());
        assertTrue(user.isVerified());
        assertEquals("GroupA", user.getGroupName());
        assertEquals(2, user.getStudyYear());
        assertEquals(1, user.getSemester());
        assertEquals(courses, user.getCourses());
    }

    @Test
    void testUserRoleAssignment() {
        User user = new User();
        user.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }
}
