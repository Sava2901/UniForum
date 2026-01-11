package com.forum.repository;

import com.forum.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByForumIdOrderByIsPinnedDescScoreDescTimestampDesc(Long forumId);
    
}
