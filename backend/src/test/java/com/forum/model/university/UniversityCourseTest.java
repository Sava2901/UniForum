package com.forum.model.university;

import com.forum.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class UniversityCourseTest {

    @Test
    void testNoArgsConstructor() {
        UniversityCourse course = new UniversityCourse();
        assertNotNull(course);
        assertNull(course.getId());
    }

    @Test
    void testSetterGetter() {
        UniversityCourse course = new UniversityCourse();
        course.setId(1L);
        course.setName("Math");
        course.setYear(1);
        course.setSemester(2);
        course.setDescription("Mathematics Course");
        
        assertEquals(1L, course.getId());
        assertEquals("Math", course.getName());
        assertEquals(1, course.getYear());
        assertEquals(2, course.getSemester());
        assertEquals("Mathematics Course", course.getDescription());
    }

    @Test
    void testEnrolledUsers() {
        UniversityCourse course = new UniversityCourse();
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        users.add(user);
        
        course.setEnrolledUsers(users);
        assertEquals(1, course.getEnrolledUsers().size());
        assertEquals(1L, course.getEnrolledUsers().get(0).getId());
    }
}
