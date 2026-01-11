package com.forum.config;

import com.forum.model.university.UniversityCourse;
import com.forum.model.university.UniversityStudent;
import com.forum.repository.university.UniversityCourseRepository;
import com.forum.repository.university.UniversityStudentRepository;
import com.forum.service.AdminService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.forum.model.university.UniversityProfessor;
import com.forum.repository.university.UniversityProfessorRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUniversityData(UniversityStudentRepository studentRepo, 
                                         UniversityCourseRepository courseRepo,
                                         UniversityProfessorRepository profRepo,
                                         AdminService adminService) {
        return args -> {
            
            createProfessorIfNotFound(profRepo, "prof.math@uni.edu", "John", "Nash", "Mathematics");
            createProfessorIfNotFound(profRepo, "prof.cs@uni.edu", "Alan", "Turing", "Computer Science");
            createProfessorIfNotFound(profRepo, "prof.physics@uni.edu", "Albert", "Einstein", "Physics");
            createProfessorIfNotFound(profRepo, "prof.history@uni.edu", "Herodotus", "Of Halicarnassus", "History");
            createProfessorIfNotFound(profRepo, "prof.chemistry@uni.edu", "Marie", "Curie", "Chemistry");
            createProfessorIfNotFound(profRepo, "prof.literature@uni.edu", "William", "Shakespeare", "Literature");

            System.out.println("University Professors initialized.");

            
            createStudentIfNotFound(studentRepo, "alice@uni.edu", 1, 1, "Group A");
            createStudentIfNotFound(studentRepo, "bob@uni.edu", 1, 1, "Group B");
            createStudentIfNotFound(studentRepo, "charlie@uni.edu", 2, 1, "Group A");
            createStudentIfNotFound(studentRepo, "diana@uni.edu", 2, 1, "Group B");
            createStudentIfNotFound(studentRepo, "eve@uni.edu", 1, 2, "Group A");
            createStudentIfNotFound(studentRepo, "frank@uni.edu", 3, 1, "Group C");

            System.out.println("University Students initialized.");

            
            createCourseIfNotFound(courseRepo, "Math 101", 1, 1, "Introduction to Mathematics");
            createCourseIfNotFound(courseRepo, "CS 101", 1, 1, "Introduction to Computer Science");
            createCourseIfNotFound(courseRepo, "Physics 101", 1, 1, "Fundamentals of Physics");
            createCourseIfNotFound(courseRepo, "History 101", 1, 1, "World History Overview");
            createCourseIfNotFound(courseRepo, "Math 201", 2, 1, "Advanced Calculus");
            createCourseIfNotFound(courseRepo, "CS 201", 2, 1, "Data Structures and Algorithms");
            createCourseIfNotFound(courseRepo, "Chemistry 101", 1, 1, "General Chemistry");
            createCourseIfNotFound(courseRepo, "Literature 101", 1, 2, "Introduction to Literature");

            System.out.println("University Courses initialized.");
            
            
            try {
                
                
                
                adminService.syncProfessors(); 
                System.out.println("Professors synced to Users DB.");
                
                
                
                adminService.initializeForums();
                System.out.println("Forums initialized.");
            } catch (Exception e) {
                System.err.println("Failed to initialize forums: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    private void createProfessorIfNotFound(UniversityProfessorRepository repo, String email, String firstName, String lastName, String department) {
        if (repo.findByEmail(email).isEmpty()) {
            UniversityProfessor p = new UniversityProfessor();
            p.setEmail(email);
            p.setFirstName(firstName);
            p.setLastName(lastName);
            p.setDepartment(department);
            repo.save(p);
        }
    }

    private void createStudentIfNotFound(UniversityStudentRepository repo, String email, int year, int semester, String groupName) {
        if (repo.findByEmail(email).isEmpty()) {
            UniversityStudent s = new UniversityStudent();
            s.setEmail(email);
            s.setYear(year);
            s.setSemester(semester);
            s.setGroupName(groupName);
            repo.save(s);
        }
    }

    private void createCourseIfNotFound(UniversityCourseRepository repo, String name, int year, int semester, String description) {
        if (repo.findByName(name).isEmpty()) {
            UniversityCourse c = new UniversityCourse();
            c.setName(name);
            c.setYear(year);
            c.setSemester(semester);
            c.setDescription(description);
            repo.save(c);
        }
    }
}
