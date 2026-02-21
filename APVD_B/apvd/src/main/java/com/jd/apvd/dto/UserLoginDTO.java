package com.jd.apvd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {
    
    @NotBlank(message = "User ID or Email is required")
    private String userIdOrEmail;  // Can be userID or userEmail
    
    @NotBlank(message = "Password is required")
    private String password;
}
