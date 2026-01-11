package com.forum.service;

import com.forum.dto.RegisterRequest;
import com.forum.model.User;
import com.forum.model.Role;
import com.forum.model.university.UniversityCourse;
import com.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service for user management.
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private com.forum.repository.university.UniversityStudentRepository uniStudentRepo;
    @Autowired
    private com.forum.repository.university.UniversityCourseRepository uniCourseRepo;
    @Autowired
    private ForumService forumService;

    /**
     * Registers a new user and links to university data if available.
     */
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Default role is STUDENT
        user.setRole(Role.STUDENT); 
        
        // Check if student exists in university DB
        var uniStudent = uniStudentRepo.findByEmail(request.getEmail());
        
        if (uniStudent.isPresent()) {
            // Auto-verify if found in university DB
            user.setVerified(true);
            user.setStudyYear(uniStudent.get().getYear());
            user.setSemester(uniStudent.get().getSemester());
            user.setGroupName(uniStudent.get().getGroupName());

            // Courses will be linked below
            
        } else {
            // Require email verification
            user.setVerified(false);
        }

        // Nickname check
        if (request.getNickname() == null || request.getNickname().isBlank()) {
             throw new RuntimeException("Nickname is required for students");
        }
        user.setNickname(request.getNickname());
        
        User savedUser = userRepository.save(user);

        // Link courses if verified
        if (savedUser.isVerified() && savedUser.getRole() == Role.STUDENT) {
            var uniStudentOpt = uniStudentRepo.findByEmail(request.getEmail());
            if (uniStudentOpt.isPresent()) {
                List<UniversityCourse> courses = uniCourseRepo.findByYearAndSemester(
                    uniStudentOpt.get().getYear(), 
                    uniStudentOpt.get().getSemester()
                );
                
                for (UniversityCourse course : courses) {
                    if (course.getEnrolledUsers() == null) {
                        course.setEnrolledUsers(new java.util.ArrayList<>());
                    }
                    if (!course.getEnrolledUsers().contains(savedUser)) {
                        course.getEnrolledUsers().add(savedUser);
                        uniCourseRepo.save(course);
                        
                        // Ensure group subforum exists
                        if (savedUser.getGroupName() != null) {
                            forumService.createGroupSubforumIfMissing(course, savedUser.getGroupName());
                        }
                    }
                }
            }
        }

        if (!savedUser.isVerified()) {
            emailService.sendVerificationEmail(savedUser.getEmail());
        }
        return savedUser;
    }
    
    /**
     * Retrieves unverified users.
     */
    public List<User> getPendingUsers() {
        return userRepository.findByVerifiedFalse();
    }
    
    /**
     * Retrieves users with professor role.
     */
    public List<User> getProfessors() {
        return userRepository.findByRole(Role.PROFESSOR);
    }
    
    /**
     * Marks a user as verified.
     */
    public void verifyUser(Long userId) {
        if (userId == null) throw new RuntimeException("User ID cannot be null");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        User savedUser = userRepository.save(user);
        emailService.sendVerificationEmail(savedUser.getEmail());
    }
    
    /**
     * Finds a user by email.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Retrieves all users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Syncs student data with university records.
     */
    @Transactional
    public void syncStudentData(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getRole() != Role.STUDENT) return;
        
        uniStudentRepo.findByEmail(email).ifPresent(uniStudent -> {
            boolean changed = false;
            if (user.getStudyYear() == null || !user.getStudyYear().equals(uniStudent.getYear())) {
                user.setStudyYear(uniStudent.getYear());
                changed = true;
            }
            if (user.getSemester() == null || !user.getSemester().equals(uniStudent.getSemester())) {
                user.setSemester(uniStudent.getSemester());
                changed = true;
            }
            if (user.getGroupName() == null || !user.getGroupName().equals(uniStudent.getGroupName())) {
                user.setGroupName(uniStudent.getGroupName());
                changed = true;
                
                // Update forums for new group
                if (user.getCourses() != null) {
                    // Force load
                    user.getCourses().size(); 
                    for (UniversityCourse course : user.getCourses()) {
                        forumService.createGroupSubforumIfMissing(course, user.getGroupName());
                    }
                }
            }
            
            if (!user.isVerified()) {
                user.setVerified(true);
                changed = true;
            }
            
            if (changed) {
                userRepository.save(user);
            }
        });
    }
}
