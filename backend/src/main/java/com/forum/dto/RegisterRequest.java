package com.forum.dto;

import com.forum.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;
    
    private String nickname; // Required for students
    
    private Role role; // User selects their role (Admin approves it)
    
    private Long groupId; // For students
}
