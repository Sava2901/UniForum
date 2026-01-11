package com.forum.controller;

import com.forum.model.User;
import com.forum.model.university.UniversityCourse;
import com.forum.service.UserService;
import com.forum.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Map;

/**
 * Controller for administrative tasks (user management, course management).
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private AdminService adminService;
    
    /**
     * Retrieves users waiting for verification.
     * @return List of pending users.
     */
    @GetMapping("/users/pending")
    public ResponseEntity<List<User>> getPendingUsers() {
        return ResponseEntity.ok(userService.getPendingUsers());
    }
    
    /**
     * Retrieves all professors.
     * @return List of professors.
     */
    @GetMapping("/users/professors")
    public ResponseEntity<List<User>> getProfessors() {
        return ResponseEntity.ok(userService.getProfessors());
    }
    
    /**
     * Retrieves all student group names.
     * @return List of group names.
     */
    @GetMapping("/groups")
    public ResponseEntity<List<String>> getAllGroups() {
        return ResponseEntity.ok(adminService.getAllGroupNames());
    }
    
    /**
     * Verifies a user account.
     * @param userId The ID of the user to verify.
     * @return Success message.
     */
    @PostMapping("/users/{userId}/verify")
    public ResponseEntity<?> verifyUser(@PathVariable Long userId) {
        userService.verifyUser(userId);
        return ResponseEntity.ok("User verified");
    }
    
    /**
     * Creates a new university course.
     * @param course The course details.
     * @return The created course.
     */
    @PostMapping("/courses")
    public ResponseEntity<UniversityCourse> createCourse(@RequestBody @NonNull UniversityCourse course) {
        return ResponseEntity.ok(adminService.createUniversityCourse(course));
    }
    
    /**
     * Moves a student to a different group.
     * @param email The email of the student.
     * @param payload Map containing the new group name.
     * @return Success message.
     */
    @PostMapping("/users/{email}/move")
    public ResponseEntity<?> moveStudent(@PathVariable String email, @RequestBody Map<String, String> payload) {
        String groupName = payload.get("groupName");
        adminService.moveStudent(email, groupName);
        return ResponseEntity.ok("Student moved successfully");
    }
    
    /**
     * Retrieves all users.
     * @return List of all users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    /**
     * Updates user details.
     * @param userId The ID of the user.
     * @param user The updated user details.
     * @return Success message.
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User user) {
        adminService.updateUser(userId, user);
        return ResponseEntity.ok("User updated successfully");
    }
    
    /**
     * Enrolls a student in a course.
     * @param courseId The ID of the course.
     * @param userId The ID of the student.
     * @return Success message.
     */
    @PostMapping("/courses/{courseId}/enroll/{userId}")
    public ResponseEntity<?> enrollStudent(@PathVariable Long courseId, @PathVariable Long userId) {
        adminService.enrollStudentInCourse(userId, courseId);
        return ResponseEntity.ok("Student enrolled successfully");
    }

    /**
     * Removes a student from a course.
     * @param courseId The ID of the course.
     * @param userId The ID of the student.
     * @return Success message.
     */
    @DeleteMapping("/courses/{courseId}/enroll/{userId}")
    public ResponseEntity<?> removeStudentFromCourse(@PathVariable Long courseId, @PathVariable Long userId) {
        adminService.removeStudentFromCourse(userId, courseId);
        return ResponseEntity.ok("Student removed from course successfully");
    }

    /**
     * Assigns a student to a specific forum.
     * @param forumId The ID of the forum.
     * @param userId The ID of the student.
     * @return Success message.
     */
    @PostMapping("/forums/{forumId}/enroll/{userId}")
    public ResponseEntity<?> assignStudentToForum(@PathVariable Long forumId, @PathVariable Long userId) {
        adminService.assignStudentToForum(userId, forumId);
        return ResponseEntity.ok("Student assigned to forum successfully");
    }
    
    /**
     * Assigns a professor to a forum.
     * @param forumId The ID of the forum.
     * @param professorId The ID of the professor.
     * @return Success message.
     */
    @PostMapping("/forums/{forumId}/assign")
    public ResponseEntity<?> assignProfessor(@NonNull @PathVariable Long forumId, @NonNull @RequestParam Long professorId) {
        adminService.assignProfessor(forumId, professorId);
        return ResponseEntity.ok("Professor assigned successfully");
    }

    /**
     * Removes a professor from a forum.
     * @param forumId The ID of the forum.
     * @return Success message.
     */
    @DeleteMapping("/forums/{forumId}/professor")
    public ResponseEntity<?> removeProfessorFromForum(@PathVariable Long forumId) {
        adminService.removeProfessorFromForum(forumId);
        return ResponseEntity.ok("Professor removed from forum successfully");
    }

    /**
     * Deletes a user account.
     * @param userId The ID of the user.
     * @return Success message.
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}
