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

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/users/pending")
    public ResponseEntity<List<User>> getPendingUsers() {
        return ResponseEntity.ok(userService.getPendingUsers());
    }
    
    @GetMapping("/users/professors")
    public ResponseEntity<List<User>> getProfessors() {
        return ResponseEntity.ok(userService.getProfessors());
    }
    
    @GetMapping("/groups")
    public ResponseEntity<List<String>> getAllGroups() {
        return ResponseEntity.ok(adminService.getAllGroupNames());
    }
    
    @PostMapping("/users/{userId}/verify")
    public ResponseEntity<?> verifyUser(@PathVariable Long userId) {
        userService.verifyUser(userId);
        return ResponseEntity.ok("User verified");
    }
    
    @PostMapping("/courses")
    public ResponseEntity<UniversityCourse> createCourse(@RequestBody @NonNull UniversityCourse course) {
        return ResponseEntity.ok(adminService.createUniversityCourse(course));
    }
    
    // Groups are now managed via strings/University DB, so explicit creation is removed or simulated
    // We can remove this endpoint or make it do nothing if frontend still calls it
    // @PostMapping("/groups") ...
    
    @PostMapping("/users/{email}/move")
    public ResponseEntity<?> moveStudent(@PathVariable String email, @RequestBody Map<String, String> payload) {
        String groupName = payload.get("groupName");
        adminService.moveStudent(email, groupName);
        return ResponseEntity.ok("Student moved successfully");
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // We can reuse getPendingUsers() logic but for all, or add a method in UserService
        // For simplicity, let's just return all users here or add a method
        // But userService.getPendingUsers() only returns unverified.
        // Let's assume we want ALL users. 
        // I need to add getAllUsers to UserService or repository access.
        // Let's stick to what we have or add a quick repository call if possible, 
        // but better to use service.
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User user) {
        adminService.updateUser(userId, user);
        return ResponseEntity.ok("User updated successfully");
    }
    
    @PostMapping("/courses/{courseId}/enroll/{userId}")
    public ResponseEntity<?> enrollStudent(@PathVariable Long courseId, @PathVariable Long userId) {
        adminService.enrollStudentInCourse(userId, courseId);
        return ResponseEntity.ok("Student enrolled successfully");
    }

    @DeleteMapping("/courses/{courseId}/enroll/{userId}")
    public ResponseEntity<?> removeStudentFromCourse(@PathVariable Long courseId, @PathVariable Long userId) {
        adminService.removeStudentFromCourse(userId, courseId);
        return ResponseEntity.ok("Student removed from course successfully");
    }

    @PostMapping("/forums/{forumId}/enroll/{userId}")
    public ResponseEntity<?> assignStudentToForum(@PathVariable Long forumId, @PathVariable Long userId) {
        adminService.assignStudentToForum(userId, forumId);
        return ResponseEntity.ok("Student assigned to forum successfully");
    }
    
    @PostMapping("/forums/{forumId}/assign")
    public ResponseEntity<?> assignProfessor(@NonNull @PathVariable Long forumId, @NonNull @RequestParam Long professorId) {
        adminService.assignProfessor(forumId, professorId);
        return ResponseEntity.ok("Professor assigned successfully");
    }

    @DeleteMapping("/forums/{forumId}/professor")
    public ResponseEntity<?> removeProfessorFromForum(@PathVariable Long forumId) {
        adminService.removeProfessorFromForum(forumId);
        return ResponseEntity.ok("Professor removed from forum successfully");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}
