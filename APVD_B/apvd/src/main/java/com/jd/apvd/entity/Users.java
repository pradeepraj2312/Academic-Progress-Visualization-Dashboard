package com.jd.apvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    
    @Id
    @Column(unique = true, nullable = false)
    private String userId;  // User-provided unique ID (e.g., APVD001, E001, etc.)
    
    @Column(nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String userEmail;
    
    @Column(nullable = false)
    private String userPassword;
    
    @Column(nullable = false)
    private String mobile;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;  // STUDENT, FACULTY, ADMIN
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
