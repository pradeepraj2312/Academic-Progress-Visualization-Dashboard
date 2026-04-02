package com.jd.apvd.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMarksDTO {
    
    private Long id;
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    private String username;
    private String userEmail;
    private String enrollmentNumber;
    private String department;
    private Integer yearOfStudying;
    
    @NotNull(message = "Semester is required")
    @Min(1)
    @Max(8)
    private Integer semester;
    
    @NotNull(message = "Subject 1 Mark is required")
    @Min(0)
    @Max(100)
    private Double subject1Mark;

    private String subject1Name;
    
    @NotNull(message = "Subject 2 Mark is required")
    @Min(0)
    @Max(100)
    private Double subject2Mark;

    private String subject2Name;
    
    @NotNull(message = "Subject 3 Mark is required")
    @Min(0)
    @Max(100)
    private Double subject3Mark;

    private String subject3Name;
    
    @NotNull(message = "Subject 4 Mark is required")
    @Min(0)
    @Max(100)
    private Double subject4Mark;

    private String subject4Name;
    
    @NotNull(message = "Subject 5 Mark is required")
    @Min(0)
    @Max(100)
    private Double subject5Mark;

    private String subject5Name;
    
    @NotNull(message = "Subject 6 Mark is required")
    @Min(0)
    @Max(100)
    private Double subject6Mark;

    private String subject6Name;
    
    private Double totalMarks;
    private Double sgpa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
