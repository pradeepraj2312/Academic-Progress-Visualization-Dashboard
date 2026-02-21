package com.jd.apvd.dto;

import com.jd.apvd.entity.AttendanceStatus;
import com.jd.apvd.entity.Session;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchAttendanceDTO {
    
    @NotNull(message = "Attendance Date is required")
    private LocalDate attendanceDate;
    
    @NotNull(message = "Session is required")
    private Session session;  // FN, AN
    
    @NotNull(message = "Student IDs list is required")
    private List<String> studentIds;
    
    @NotNull(message = "Status is required")
    private AttendanceStatus status;  // PRESENT, ABSENT
    
    private String remarks;
}
