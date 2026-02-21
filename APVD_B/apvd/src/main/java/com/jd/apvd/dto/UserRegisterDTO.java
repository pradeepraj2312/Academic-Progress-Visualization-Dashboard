package com.jd.apvd.dto;

import com.jd.apvd.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {
    
    @NotBlank(message = "User ID is required")
    private String userId;  // User-provided unique ID
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String userEmail;
    
    @NotBlank(message = "Password is required")
    private String userPassword;
    
    @NotBlank(message = "Mobile number is required")
    private String mobile;
    
    private UserRole role;  // STUDENT, FACULTY, ADMIN
    
    @NotBlank(message = "Department is required")
    private String department;  // Department for all users (default: management)
    
    // Additional fields for students
    private String enrollmentNumber;
    private Integer yearOfStudying;
    
    // Additional fields for faculty
    private String qualification;
    private String specialization;
}
