package com.jd.apvd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    
    private Long departmentId;
    
    @NotBlank(message = "Department name is required")
    private String departmentName;
    
    private String departmentCode;
    
    private String description;
}
