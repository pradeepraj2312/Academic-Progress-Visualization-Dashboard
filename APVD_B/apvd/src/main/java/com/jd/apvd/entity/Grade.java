package com.jd.apvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades", uniqueConstraints = {@UniqueConstraint(columnNames = {"student_user_id", "course_id", "semester"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gradeId;
    
    @Column(name = "student_user_id", nullable = false)
    private String studentUserId;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(nullable = false)
    private Integer semester;
    
    @Column(nullable = false)
    private String department;
    
    // Course details (denormalized for quick access)
    private String courseCode;
    private String courseName;
    
    @Enumerated(EnumType.STRING)
    private CourseStatus courseStatus;  // CORE or ELECTIVE
    
    // Marks and grades
    @Column(nullable = false)
    private Double marks;  // Out of 100
    
    @Column(nullable = false)
    private String letterGrade;  // A+, A, B+, B, C+, C, D, F
    
    @Column(nullable = false)
    private Double gradePoint;  // 10.0, 9.0, 8.0, etc.
    
    private Integer credits;  // Course credits
    
    // Faculty who assigned the grade
    private String gradedByFacultyId;
    
    private String remarks;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateGrade();  // Calculate grade on creation
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateGrade();  // Recalculate grade on update
    }
    
    /**
     * Auto-calculate letter grade and grade point based on marks
     */
    private void calculateGrade() {
        if (marks == null) {
            letterGrade = "N/A";
            gradePoint = 0.0;
            return;
        }
        
        if (marks >= 90) {
            letterGrade = "A+";
            gradePoint = 10.0;
        } else if (marks >= 80) {
            letterGrade = "A";
            gradePoint = 9.0;
        } else if (marks >= 70) {
            letterGrade = "B+";
            gradePoint = 8.0;
        } else if (marks >= 60) {
            letterGrade = "B";
            gradePoint = 7.0;
        } else if (marks >= 50) {
            letterGrade = "C+";
            gradePoint = 6.0;
        } else if (marks >= 40) {
            letterGrade = "C";
            gradePoint = 5.0;
        } else if (marks >= 35) {
            letterGrade = "D";
            gradePoint = 4.0;
        } else {
            letterGrade = "F";
            gradePoint = 0.0;
        }
    }
}
