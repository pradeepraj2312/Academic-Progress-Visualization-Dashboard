package com.jd.apvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;
    
    @Column(unique = true, nullable = false)
    private String courseCode;  // e.g., CS101, CS102
    
    @Column(nullable = false)
    private String courseName;
    
    @Column(nullable = false)
    private String department;  // e.g., Computer Science, Electronics
    
    @Column(nullable = false)
    private Integer semester;  // 1-8
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus courseStatus;  // CORE or ELECTIVE
    
    @Column(nullable = false)
    private String facultyUserId;  // Faculty who created/owns the course
    
    private String description;
    
    private Integer credits;  // Course credits (e.g., 3, 4)
    
    private Integer capacity;  // Maximum students allowed
    
    private Integer enrolledCount = 0;  // Current enrollment count
    
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
