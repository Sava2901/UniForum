package com.forum.dto;

import com.forum.model.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthorDtoTest {

    @Test
    void testNoArgsConstructor() {
        AuthorDto authorDto = new AuthorDto();
        assertNotNull(authorDto);
        assertNull(authorDto.getId());
        assertNull(authorDto.getDisplayName());
        assertNull(authorDto.getRole());
        assertNull(authorDto.getEmail());
    }

    @Test
    void testAllArgsConstructor() {
        AuthorDto authorDto = new AuthorDto(1L, "John Doe", Role.PROFESSOR, "john@example.com");
        
        assertEquals(1L, authorDto.getId());
        assertEquals("John Doe", authorDto.getDisplayName());
        assertEquals(Role.PROFESSOR, authorDto.getRole());
        assertEquals("john@example.com", authorDto.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        AuthorDto authorDto = new AuthorDto();
        authorDto.setId(2L);
        authorDto.setDisplayName("Jane");
        authorDto.setRole(Role.STUDENT);
        authorDto.setEmail(null);

        assertEquals(2L, authorDto.getId());
        assertEquals("Jane", authorDto.getDisplayName());
        assertEquals(Role.STUDENT, authorDto.getRole());
        assertNull(authorDto.getEmail());
    }
}
