package com.jd.apvd.dto;

import com.jd.apvd.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String userId;
    private String username;
    private String userEmail;
    private String mobile;
    private UserRole role;
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
}
