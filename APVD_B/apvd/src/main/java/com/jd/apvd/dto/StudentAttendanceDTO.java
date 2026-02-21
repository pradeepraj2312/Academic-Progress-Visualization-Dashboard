package com.jd.apvd.dto;

import com.jd.apvd.entity.AttendanceStatus;
import com.jd.apvd.entity.Session;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceDTO {
    
    private Long id;
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    private String username;
    private String userEmail;
    private String enrollmentNumber;
    private String department;
    private Integer yearOfStudying;
    
    @NotNull(message = "Attendance Date is required")
    private LocalDate attendanceDate;
    
    @NotNull(message = "Session is required")
    private Session session;  // FN, AN
    
    @NotNull(message = "Status is required")
    private AttendanceStatus status;  // PRESENT, ABSENT
    
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
