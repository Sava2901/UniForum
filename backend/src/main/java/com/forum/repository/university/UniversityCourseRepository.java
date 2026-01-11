package com.forum.repository.university;

import com.forum.model.university.UniversityCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UniversityCourseRepository extends JpaRepository<UniversityCourse, Long> {
    List<UniversityCourse> findByYearAndSemester(int year, int semester);
    java.util.Optional<UniversityCourse> findByName(String name);
}
