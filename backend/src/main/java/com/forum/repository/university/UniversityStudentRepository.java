package com.forum.repository.university;

import com.forum.model.university.UniversityStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UniversityStudentRepository extends JpaRepository<UniversityStudent, Long> {
    Optional<UniversityStudent> findByEmail(String email);
}
