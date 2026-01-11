package com.forum.repository;

import com.forum.model.university.UniversityCourse;
import com.forum.model.Forum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ForumRepository extends JpaRepository<Forum, Long> {
    List<Forum> findByCourseId(Long courseId);
    List<Forum> findByGroupName(String groupName);
    Optional<Forum> findByCourseAndType(UniversityCourse course, Forum.ForumType type);
    Optional<Forum> findByCourseAndGroupNameAndType(UniversityCourse course, String groupName, Forum.ForumType type);
    
    // For getting all forums visible to a user
    List<Forum> findByCourseInAndType(List<UniversityCourse> courses, Forum.ForumType type);
    List<Forum> findByCourseInAndGroupNameAndType(List<UniversityCourse> courses, String groupName, Forum.ForumType type);
    
    // For professors
    List<Forum> findByProfessorId(Long professorId);
}
