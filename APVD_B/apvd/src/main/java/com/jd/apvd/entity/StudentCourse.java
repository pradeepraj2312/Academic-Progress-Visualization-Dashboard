package com.jd.apvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_courses", uniqueConstraints = {@UniqueConstraint(columnNames = {"student_user_id", "course_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_user_id", nullable = false)
    private String studentUserId;  // Reference to student's userId
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;  // Reference to Course.courseId
    
    @Column(nullable = false)
    private Integer semester;  // Student's current semester
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus courseStatus;  // CORE or ELECTIVE (for validation)
    
    @Column(nullable = false)
    private Boolean isActive = true;  // Whether the enrollment is active
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
