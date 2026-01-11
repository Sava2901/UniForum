package com.forum.controller;

import com.forum.dto.RegisterRequest;
import com.forum.dto.LoginRequest;
import com.forum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.forum.dto.JwtResponse;
import com.forum.security.jwt.JwtUtils;
import com.forum.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// @CrossOrigin(origins = "http://localhost:5173") // Handled globally in SecurityConfig
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Sync data first if applicable
            userService.syncStudentData(request.getEmail());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            // Fetch full user details to get fields not in UserDetailsImpl if needed, 
            // but UserDetailsImpl should be enough or we can fetch from DB.
            // Let's rely on UserDetailsImpl + what we know or fetch DB if nickname is missing from UserDetailsImpl
            // Actually, my UserDetailsImpl doesn't store nickname/groupName etc.
            // So I should fetch the User entity.
            
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
