package com.forum.controller;

import com.forum.dto.RegisterRequest;
import com.forum.dto.LoginRequest;
import com.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.forum.dto.JwtResponse;
import com.forum.security.jwt.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication requests (registration, login).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Registers a new user.
     * @param request The registration details.
     * @return The registered user or an error message.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Authenticates a user and generates a JWT token.
     * @param request The login credentials.
     * @return A JWT response containing the token and user details.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Sync student data with university database if applicable
            userService.syncStudentData(request.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            var user = userService.findByEmail(request.getEmail());
            
            return ResponseEntity.ok(new JwtResponse(jwt, 
                    user.getId(), 
                    user.getEmail(), 
                    user.getFirstName(), 
                    user.getLastName(), 
                    user.getRole(),
                    user.getNickname(),
                    user.getGroupName(),
                    user.getStudyYear(),
                    user.getSemester()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials or account not verified");
        }
    }
}
