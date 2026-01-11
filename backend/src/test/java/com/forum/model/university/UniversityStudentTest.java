package com.forum.model.university;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UniversityStudentTest {

    @Test
    void testNoArgsConstructor() {
        UniversityStudent student = new UniversityStudent();
        assertNotNull(student);
        assertNull(student.getId());
        assertEquals(0, student.getYear());
    }

    @Test
    void testSettersAndGetters() {
        UniversityStudent student = new UniversityStudent();
        student.setId(2L);
        student.setEmail("student@univ.edu");
        student.setYear(3);
        student.setSemester(2);
        student.setGroupName("Group A");

        assertEquals(2L, student.getId());
        assertEquals("student@univ.edu", student.getEmail());
        assertEquals(3, student.getYear());
        assertEquals(2, student.getSemester());
        assertEquals("Group A", student.getGroupName());
    }

    @Test
    void testToString() {
        UniversityStudent student = new UniversityStudent();
        student.setEmail("test@test.com");
        
        String str = student.toString();
        assertTrue(str.contains("test@test.com"));
    }
}
