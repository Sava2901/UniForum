package com.forum.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoteTest {

    @Test
    void testNoArgsConstructor() {
        Vote vote = new Vote();
        assertNotNull(vote);
        assertNull(vote.getId());
        assertEquals(0, vote.getValue());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User();
        user.setId(3L);
        Post post = new Post();
        post.setId(30L);
        Comment comment = null; // Voting on a post, so comment is null

        Vote vote = new Vote(1L, user, post, comment, 1);

        assertEquals(1L, vote.getId());
        assertEquals(user, vote.getUser());
        assertEquals(post, vote.getPost());
        assertNull(vote.getComment());
        assertEquals(1, vote.getValue());
    }

    @Test
    void testVoteValueValidation() {
        // While the model doesn't strictly enforce 1/-1 in the setter without custom logic or validation annotations that run at runtime,
        // we can at least test that we can set these values.
        Vote vote = new Vote();
        vote.setValue(1);
        assertEquals(1, vote.getValue());
        
        vote.setValue(-1);
        assertEquals(-1, vote.getValue());
    }
}
