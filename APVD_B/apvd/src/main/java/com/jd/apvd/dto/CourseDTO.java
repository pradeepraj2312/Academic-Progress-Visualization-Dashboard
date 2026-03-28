package com.jd.apvd.dto;

import com.jd.apvd.entity.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    
    private Long courseId;
    
    @NotBlank(message = "Course code is required")
    private String courseCode;
    
    @NotBlank(message = "Course name is required")
    private String courseName;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotNull(message = "Semester is required")
    private Integer semester;
    
    @NotNull(message = "Course status is required (CORE or ELECTIVE)")
    private CourseStatus courseStatus;
    
    @NotBlank(message = "Faculty user ID is required")
    private String facultyUserId;
    
    private String description;
    
    private Integer credits;

    private Integer capacity;
    
    private Integer enrolledCount;
}
