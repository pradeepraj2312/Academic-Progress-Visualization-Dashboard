package com.jd.apvd.dto;

import com.jd.apvd.entity.CourseStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {
    
    private Long gradeId;
    
    @NotBlank(message = "Student user ID is required")
    private String studentUserId;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Semester is required")
    private Integer semester;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    private String courseCode;
    
    private String courseName;
    
    private CourseStatus courseStatus;
    
    @NotNull(message = "Marks are required")
    @Min(value = 0, message = "Marks must be at least 0")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private Double marks;
    
    private String letterGrade;  // Auto-calculated
    
    private Double gradePoint;  // Auto-calculated
    
    private Integer credits;
    
    private String gradedByFacultyId;
    
    private String remarks;
}
