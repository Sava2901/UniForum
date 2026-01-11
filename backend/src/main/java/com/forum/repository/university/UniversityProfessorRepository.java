package com.forum.repository.university;

import com.forum.model.university.UniversityProfessor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UniversityProfessorRepository extends JpaRepository<UniversityProfessor, Long> {
    Optional<UniversityProfessor> findByEmail(String email);
}