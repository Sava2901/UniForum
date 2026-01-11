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

    public void moveStudent(String studentEmail, String newGroupName) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        user.setGroupName(newGroupName);
        userRepository.save(user);
        
        // Ensure subforums exist for all enrolled courses with the new group
        if (user.getCourses() != null) {
            // Force init if lazy
            user.getCourses().size();
            for (UniversityCourse course : user.getCourses()) {
                forumService.createGroupSubforumIfMissing(course, newGroupName);
            }
        }
    }
    
    public void assignProfessor(Long forumId, Long professorId) {
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
    
    public void removeProfessorFromForum(Long forumId) {
        Forum forum = forumRepository.findById(forumId)
                .orElseThrow(() -> new RuntimeException("Forum not found"));
        
        forum.setProfessor(null);
        forumRepository.save(forum);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Remove from courses enrolled
        if (user.getCourses() != null) {
            for (UniversityCourse course : user.getCourses()) {
                course.getEnrolledUsers().remove(user);
                uniCourseRepo.save(course);
            }
        }
        
        // Remove from assigned forums (as professor)
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

    public void removeStudentFromCourse(Long userId, Long courseId) {
        UniversityCourse course = uniCourseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (course.getEnrolledUsers() != null) {
            // Remove by ID to ensure it works even if object references differ
            boolean removed = course.getEnrolledUsers().removeIf(u -> u.getId().equals(student.getId()));
            
            if (removed) {
                uniCourseRepo.save(course);
                
                // Also explicitly remove from student's course list if initialized (inverse side)
                if (student.getCourses() != null) {
                    student.getCourses().removeIf(c -> c.getId().equals(course.getId()));
                }
            }
        }
    }

    public void assignStudentToForum(Long userId, Long forumId) {
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
    
    public UniversityCourse createUniversityCourse(@NonNull UniversityCourse course) {
        return uniCourseRepo.save(course);
    }
    
    public void enrollStudentInCourse(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        UniversityCourse course = uniCourseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getEnrolledUsers() == null) {
            course.setEnrolledUsers(new java.util.ArrayList<>());
        }
        
        // Check by ID to be safe
        boolean alreadyEnrolled = course.getEnrolledUsers().stream()
                .anyMatch(u -> u.getId().equals(student.getId()));
                
        if (!alreadyEnrolled) {
            course.getEnrolledUsers().add(student);
            uniCourseRepo.save(course);
            
            // Update inverse side for consistency
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
    
    public void updateUser(Long userId, User updatedData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Track changes
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
        
        // If group changed, ensure subforums exist for all enrolled courses
        if (groupChanged && user.getRole() == Role.STUDENT && user.getCourses() != null) {
            // Force init if lazy
            user.getCourses().size();
            for (UniversityCourse course : user.getCourses()) {
                forumService.createGroupSubforumIfMissing(course, user.getGroupName());
            }
        }
    }

    public void enrollStudents() {
        List<UniversityCourse> courses = uniCourseRepo.findAll();
        List<User> students = userRepository.findByRole(Role.STUDENT);
        
        for (UniversityCourse course : courses) {
            boolean changed = false;
            if (course.getEnrolledUsers() == null) {
                course.setEnrolledUsers(new java.util.ArrayList<>());
            }
            
            for (User student : students) {
                // Check if student matches course year and semester
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
    
    public List<String> getAllGroupNames() {
        return uniStudentRepo.findAll().stream()
                .map(com.forum.model.university.UniversityStudent::getGroupName)
                .distinct()
                .collect(Collectors.toList());
    }
    
    public void clearForums() {
        forumRepository.deleteAll();
    }

    public void initializeForums() {
        // Sync Groups from University DB to App DB (Just group names now)
        List<com.forum.model.university.UniversityStudent> allStudents = uniStudentRepo.findAll();
        List<String> groupNames = allStudents.stream()
                .map(com.forum.model.university.UniversityStudent::getGroupName)
                .distinct()
                .collect(Collectors.toList());
        
        // For each course in the App DB
        List<UniversityCourse> courses = uniCourseRepo.findAll();
        
        for (UniversityCourse course : courses) {
            // 1. Create Main Course Forum if not exists
            if (forumRepository.findByCourseAndType(course, Forum.ForumType.MAIN_COURSE).isEmpty()) {
                Forum mainForum = new Forum();
                mainForum.setCourse(course);
                mainForum.setType(Forum.ForumType.MAIN_COURSE);
                forumRepository.save(mainForum);
            }
            
            // 2. Create Sub-forums for each Group enrolled in this course
            // Logic: Group is enrolled if at least one student in that group is in the course's year/semester
            for (String groupName : groupNames) {
                // Find a representative student for this group
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
