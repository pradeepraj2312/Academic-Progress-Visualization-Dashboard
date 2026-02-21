package com.jd.apvd.dto;

import com.jd.apvd.entity.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseSelectionDTO {
    
    @NotNull(message = "Student user ID is required")
    private String studentUserId;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Semester is required")
    private Integer semester;
    
    private String courseName;
    
    private String courseCode;
    
    private CourseStatus courseStatus;
    
    private String department;
}
