package com.jd.apvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;  // Reference to student's userId
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String userEmail;
    
    @Column(nullable = false)
    private LocalDate attendanceDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Session session;  // FN, AN
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;  // PRESENT, ABSENT
    
    private String remarks;
    
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
