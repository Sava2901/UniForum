package com.forum.model;

import com.forum.model.university.UniversityCourse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForumTest {

    @Test
    void testNoArgsConstructor() {
        Forum forum = new Forum();
        assertNotNull(forum);
        assertNull(forum.getId());
    }

    @Test
    void testAllArgsConstructor() {
        UniversityCourse course = new UniversityCourse();
        course.setId(101L);
        User professor = new User();
        professor.setId(55L);
        List<Post> posts = new ArrayList<>();
        Set<User> allowedUsers = new HashSet<>();

        Forum forum = new Forum(1L, course, "GroupB", Forum.ForumType.GROUP_SUBFORUM, professor, posts, allowedUsers);
        
        assertEquals(1L, forum.getId());
        assertEquals(course, forum.getCourse());
        assertEquals("GroupB", forum.getGroupName());
        assertEquals(Forum.ForumType.GROUP_SUBFORUM, forum.getType());
        assertEquals(professor, forum.getProfessor());
        assertEquals(posts, forum.getPosts());
        assertEquals(allowedUsers, forum.getAllowedUsers());
    }

    @Test
    void testForumType() {
        Forum forum = new Forum();
        forum.setType(Forum.ForumType.MAIN_COURSE);
        assertEquals(Forum.ForumType.MAIN_COURSE, forum.getType());
        
        forum.setType(Forum.ForumType.GROUP_SUBFORUM);
        assertEquals(Forum.ForumType.GROUP_SUBFORUM, forum.getType());
    }
}
