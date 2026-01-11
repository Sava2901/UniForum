package com.forum.model.university;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UniversityProfessorTest {

    @Test
    void testNoArgsConstructor() {
        UniversityProfessor professor = new UniversityProfessor();
        assertNotNull(professor);
        assertNull(professor.getId());
        assertNull(professor.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        UniversityProfessor professor = new UniversityProfessor();
        professor.setId(1L);
        professor.setFirstName("John");
        professor.setLastName("Doe");
        professor.setEmail("john.doe@univ.edu");
        professor.setDepartment("CS");

        assertEquals(1L, professor.getId());
        assertEquals("John", professor.getFirstName());
        assertEquals("Doe", professor.getLastName());
        assertEquals("john.doe@univ.edu", professor.getEmail());
        assertEquals("CS", professor.getDepartment());
    }

    @Test
    void testEquality() {
        UniversityProfessor p1 = new UniversityProfessor();
        p1.setEmail("a@b.com");
        UniversityProfessor p2 = new UniversityProfessor();
        p2.setEmail("a@b.com");
        
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
