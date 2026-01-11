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
        
        // Public registration is ONLY for Students
        user.setRole(Role.STUDENT); 
        
        // Check University DB
        var uniStudent = uniStudentRepo.findByEmail(request.getEmail());
        
        if (uniStudent.isPresent()) {
            // Auto-verify and assign
            user.setVerified(true);
            user.setStudyYear(uniStudent.get().getYear());
            user.setSemester(uniStudent.get().getSemester());
            user.setGroupName(uniStudent.get().getGroupName());

            // Auto-enroll in courses matching year/semester
            // Logic moved to after save to ensure User has ID
        } else {
            // Not in Uni DB -> Unverified
            user.setVerified(false);
        }

        // Nickname required for students
        if (request.getNickname() == null || request.getNickname().isBlank()) {
             throw new RuntimeException("Nickname is required for students");
        }
        user.setNickname(request.getNickname());
        
        User savedUser = userRepository.save(user);

        // If verified (student found), enroll in courses
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
    
    public List<User> getPendingUsers() {
        return userRepository.findByVerifiedFalse();
    }
    
    public List<User> getProfessors() {
        return userRepository.findByRole(Role.PROFESSOR);
    }
    
    public void verifyUser(Long userId) {
        if (userId == null) throw new RuntimeException("User ID cannot be null");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(true);
        User savedUser = userRepository.save(user);
        emailService.sendVerificationEmail(savedUser.getEmail());
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

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
                
                // Group changed, ensure subforums exist for all enrolled courses
                if (user.getCourses() != null) {
                    // Force initialization if lazy
                    user.getCourses().size(); 
                    for (UniversityCourse course : user.getCourses()) {
                        forumService.createGroupSubforumIfMissing(course, user.getGroupName());
                    }
                }
            }
            // Auto-verify if found in Uni DB
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
