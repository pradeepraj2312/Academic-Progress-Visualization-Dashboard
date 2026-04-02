package com.jd.apvd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_marks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMarks {
    
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
    private Integer semester;
    
    @Column(nullable = false)
    private Double subject1Mark;

    @Column(nullable = false)
    private String subject1Name;
    
    @Column(nullable = false)
    private Double subject2Mark;

    @Column(nullable = false)
    private String subject2Name;
    
    @Column(nullable = false)
    private Double subject3Mark;

    @Column(nullable = false)
    private String subject3Name;
    
    @Column(nullable = false)
    private Double subject4Mark;

    @Column(nullable = false)
    private String subject4Name;
    
    @Column(nullable = false)
    private Double subject5Mark;

    @Column(nullable = false)
    private String subject5Name;
    
    @Column(nullable = false)
    private Double subject6Mark;

    @Column(nullable = false)
    private String subject6Name;
    
    @Column(nullable = false)
    private Double totalMarks;  // Sum of all marks
    
    @Column(nullable = false)
    private Double sgpa;  // Calculated GPA/SGPA
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        ensureDefaultSubjectNames();
        calculateTotalsAndSGPA();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        ensureDefaultSubjectNames();
        calculateTotalsAndSGPA();
    }

    private void ensureDefaultSubjectNames() {
        if (subject1Name == null || subject1Name.isBlank()) subject1Name = "Subject 1";
        if (subject2Name == null || subject2Name.isBlank()) subject2Name = "Subject 2";
        if (subject3Name == null || subject3Name.isBlank()) subject3Name = "Subject 3";
        if (subject4Name == null || subject4Name.isBlank()) subject4Name = "Subject 4";
        if (subject5Name == null || subject5Name.isBlank()) subject5Name = "Subject 5";
        if (subject6Name == null || subject6Name.isBlank()) subject6Name = "Subject 6";
    }
    
    private void calculateTotalsAndSGPA() {
        this.totalMarks = subject1Mark + subject2Mark + subject3Mark 
                        + subject4Mark + subject5Mark + subject6Mark;
        // SGPA calculation: total marks / (6 subjects * 10) * 10
        // This assumes marks are out of 100, converting to 10-point scale
        this.sgpa = Math.round((this.totalMarks / 600) * 10 * 100) / 100.0;
    }
}
