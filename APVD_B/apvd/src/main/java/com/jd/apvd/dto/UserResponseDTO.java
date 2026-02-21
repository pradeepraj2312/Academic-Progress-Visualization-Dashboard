package com.jd.apvd.dto;

import com.jd.apvd.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    
    private String userId;
    private String username;
    private String userEmail;
    private String mobile;
    private String enrollmentNumber;
    private String department;
    private Integer yearOfStudying;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
