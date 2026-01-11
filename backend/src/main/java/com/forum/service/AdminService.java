package com.forum.service;

import com.forum.model.*;
import com.forum.model.university.UniversityCourse;
import com.forum.repository.*;
import com.forum.repository.university.UniversityCourseRepository;
import com.forum.repository.university.UniversityStudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

import org.springframework.transaction.annotation.Transactional;

import com.forum.model.university.UniversityProfessor;
import com.forum.repository.university.UniversityProfessorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Service for administrative operations.
 */
@Service
@Transactional
public class AdminService {
    @Autowired
    private UniversityCourseRepository uniCourseRepo;
    @Autowired
    private ForumRepository forumRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UniversityStudentRepository uniStudentRepo;
    @Autowired
    private UniversityProfessorRepository uniProfessorRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ForumService forumService;

    /**
     * Syncs professor data from university records.
     */
    public void syncProfessors() {
        List<UniversityProfessor> uniProfessors = uniProfessorRepo.findAll();
        
        for (UniversityProfessor uniProf : uniProfessors) {
            if (userRepository.findByEmail(uniProf.getEmail()).isEmpty()) {
                User user = new User();
                user.setEmail(uniProf.getEmail());
                user.setFirstName(uniProf.getFirstName());
                user.setLastName(uniProf.getLastName());
                user.setRole(Role.PROFESSOR);
                user.setVerified(true);
                user.setPassword(passwordEncoder.encode("password")); // Default password
                user.setNickname("Prof. " + uniProf.getLastName());
                
                userRepository.save(user);
            }
        }
    }

    /**
     * Moves a student to a different group and updates forum access.
     */
    public void moveStudent(String studentEmail, String newGroupName) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        user.setGroupName(newGroupName);
        userRepository.save(user);
        
        // Update subforum access
        if (user.getCourses() != null) {
            // Force initialization if lazy loaded (though not needed with Transactional usually)
            user.getCourses().size();
            for (UniversityCourse course : user.getCourses()) {
                forumService.createGroupSubforumIfMissing(course, newGroupName);
            }
        }
    }
    
    /**
     * Assigns a professor to a forum.
     */
    public void assignProfessor(Long forumId, Long professorId) {
        if (forumId == null || professorId == null) {
            throw new IllegalArgumentException("Forum ID and Professor ID cannot be null");
        }
        Forum forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));
                
        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor not found"));
                
        if (professor.getRole() != Role.PROFESSOR && professor.getRole() != Role.ADMIN) {
            throw new RuntimeException("User is not a professor");
        }
        
        forum.setProfessor(professor);
        forumRepository.save(forum);
    }
    
    /**
     * Removes a professor from a forum.
     */
    public void removeProfessorFromForum(Long forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Forum ID cannot be null");
        }
        Forum forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));
        
        forum.setProfessor(null);
        forumRepository.save(forum);
    }

    /**
     * Deletes a user and cleans up associated data.
     */
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Remove from enrolled courses
        if (user.getCourses() != null) {
            for (UniversityCourse course : user.getCourses()) {
                course.getEnrolledUsers().remove(user);
                uniCourseRepo.save(course);
            }
        }
        
        // Remove from taught forums
        List<Forum> taughtForums = forumRepository.findByProfessorId(userId);
        for (Forum forum : taughtForums) {
            forum.setProfessor(null);
            forumRepository.save(forum);
        }
        
        // Remove from allowed users in forums
        List<Forum> allForums = forumRepository.findAll();
        for (Forum forum : allForums) {
            if (forum.getAllowedUsers() != null && forum.getAllowedUsers().contains(user)) {
                forum.getAllowedUsers().remove(user);
                forumRepository.save(forum);
            }
        }
        
        userRepository.delete(user);
    }

    /**
     * Removes a student from a course.
     */
    public void removeStudentFromCourse(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            throw new IllegalArgumentException("User ID and Course ID cannot be null");
        }
        UniversityCourse course = uniCourseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (course.getEnrolledUsers() != null) {
            // Remove user from course's enrolled list
            boolean removed = course.getEnrolledUsers().removeIf(u -> u.getId().equals(student.getId()));
            
            if (removed) {
                uniCourseRepo.save(course);
                
                // Also remove course from user's course list
                if (student.getCourses() != null) {
                    student.getCourses().removeIf(c -> c.getId().equals(course.getId()));
                }
            }
        }
    }

    /**
     * Grants a student access to a restricted forum.
     */
    public void assignStudentToForum(Long userId, Long forumId) {
        if (userId == null || forumId == null) {
            throw new IllegalArgumentException("User ID and Forum ID cannot be null");
        }
        Forum forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (forum.getAllowedUsers() == null) {
            forum.setAllowedUsers(new java.util.HashSet<>());
        }
        
        forum.getAllowedUsers().add(student);
        forumRepository.save(forum);
    }
    
    /**
     * Creates a new university course.
     */
    public UniversityCourse createUniversityCourse(@NonNull UniversityCourse course) {
        return uniCourseRepo.save(course);
    }
    
    /**
     * Enrolls a student in a course.
     */
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("Student ID and Course ID cannot be null");
        }
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        UniversityCourse course = uniCourseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getEnrolledUsers() == null) {
            course.setEnrolledUsers(new java.util.ArrayList<>());
        }
        
        // Check if already enrolled
        boolean alreadyEnrolled = course.getEnrolledUsers().stream()
                .anyMatch(u -> u.getId().equals(student.getId()));
                
        if (!alreadyEnrolled) {
            course.getEnrolledUsers().add(student);
            uniCourseRepo.save(course);
            
            // Add to student's course list as well
            if (student.getCourses() == null) {
                student.setCourses(new java.util.ArrayList<>());
            }
            student.getCourses().add(course);
            
            // Ensure group subforum exists
            if (student.getGroupName() != null) {
                forumService.createGroupSubforumIfMissing(course, student.getGroupName());
            }
        }
    }
    
    /**
     * Updates user information.
     */
    public void updateUser(Long userId, User updatedData) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if group changed
        boolean groupChanged = updatedData.getGroupName() != null && !updatedData.getGroupName().equals(user.getGroupName());
        
        user.setFirstName(updatedData.getFirstName());
        user.setLastName(updatedData.getLastName());
        user.setEmail(updatedData.getEmail());
        user.setNickname(updatedData.getNickname());
        user.setRole(updatedData.getRole());
        user.setGroupName(updatedData.getGroupName());
        user.setStudyYear(updatedData.getStudyYear());
        user.setSemester(updatedData.getSemester());
        user.setVerified(updatedData.isVerified());
        
        userRepository.save(user);
        
        // If group changed, ensure subforums exist
        if (groupChanged && user.getRole() == Role.STUDENT && user.getCourses() != null) {
            // Force init
            user.getCourses().size();
            for (UniversityCourse course : user.getCourses()) {
                forumService.createGroupSubforumIfMissing(course, user.getGroupName());
            }
        }
    }

    /**
     * Batch enrolls students based on year and semester.
     */
    public void enrollStudents() {
        List<UniversityCourse> courses = uniCourseRepo.findAll();
        List<User> students = userRepository.findByRole(Role.STUDENT);
        
        for (UniversityCourse course : courses) {
            boolean changed = false;
            if (course.getEnrolledUsers() == null) {
                course.setEnrolledUsers(new java.util.ArrayList<>());
            }
            
            for (User student : students) {
                // Match based on year and semester
                if (student.getStudyYear() != null && student.getSemester() != null &&
                    student.getStudyYear() == course.getYear() && 
                    student.getSemester() == course.getSemester()) {
                    
                    if (!course.getEnrolledUsers().contains(student)) {
                        course.getEnrolledUsers().add(student);
                        changed = true;
                    }
                }
            }
            
            if (changed) {
                uniCourseRepo.save(course);
            }
        }
    }
    
    /**
     * Retrieves unique group names.
     */
    public List<String> getAllGroupNames() {
        return uniStudentRepo.findAll().stream()
                .map(com.forum.model.university.UniversityStudent::getGroupName)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Deletes all forums.
     */
    public void clearForums() {
        forumRepository.deleteAll();
    }

    /**
     * Initializes default forums for courses and groups.
     */
    public void initializeForums() {
        // Get all unique groups
        List<com.forum.model.university.UniversityStudent> allStudents = uniStudentRepo.findAll();
        List<String> groupNames = allStudents.stream()
                .map(com.forum.model.university.UniversityStudent::getGroupName)
                .distinct()
                .collect(Collectors.toList());
        
        // For each course, create main forum and group subforums
        List<UniversityCourse> courses = uniCourseRepo.findAll();
        
        for (UniversityCourse course : courses) {
            // Main course forum
            if (forumRepository.findByCourseAndType(course, Forum.ForumType.MAIN_COURSE).isEmpty()) {
                Forum mainForum = new Forum();
                mainForum.setCourse(course);
                mainForum.setType(Forum.ForumType.MAIN_COURSE);
                forumRepository.save(mainForum);
            }
            
            // Group subforums
            // Only create if there are students in that group for that year/semester
            for (String groupName : groupNames) {
                // Check if any student in this group is in the course's year/semester
                var representativeStudent = allStudents.stream()
                        .filter(s -> s.getGroupName().equals(groupName))
                        .findFirst();
                
                if (representativeStudent.isPresent() && 
                    representativeStudent.get().getYear() == course.getYear() && 
                    representativeStudent.get().getSemester() == course.getSemester()) {
                    
                    if (forumRepository.findByCourseAndGroupNameAndType(course, groupName, Forum.ForumType.GROUP_SUBFORUM).isEmpty()) {
                        Forum subForum = new Forum();
                        subForum.setCourse(course);
                        subForum.setGroupName(groupName);
                        subForum.setType(Forum.ForumType.GROUP_SUBFORUM);
                        forumRepository.save(subForum);
                    }
                }
            }
        }
    }
}
