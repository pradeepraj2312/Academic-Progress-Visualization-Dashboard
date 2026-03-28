package com.jd.apvd.controller;

import com.jd.apvd.dto.StudentAttendanceDTO;
import com.jd.apvd.dto.BatchAttendanceDTO;
import com.jd.apvd.service.StudentAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class StudentAttendanceController {
    
    private final StudentAttendanceService attendanceService;
    
    /**
     * Mark attendance for a student
     */
    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<StudentAttendanceDTO> markAttendance(@Valid @RequestBody StudentAttendanceDTO attendanceDTO) {
        log.info("Marking attendance for userId: {} on date: {}", attendanceDTO.getUserId(), attendanceDTO.getAttendanceDate());
        StudentAttendanceDTO response = attendanceService.markAttendance(attendanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Mark attendance for multiple students at once
     */
    @PostMapping("/mark-batch")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentAttendanceDTO>> markBatchAttendance(@Valid @RequestBody BatchAttendanceDTO batchAttendanceDTO) {
        log.info("Batch marking attendance for {} students on date: {} session: {}", 
                batchAttendanceDTO.getStudentIds().size(), 
                batchAttendanceDTO.getAttendanceDate(), 
                batchAttendanceDTO.getSession());
        List<StudentAttendanceDTO> response = attendanceService.markBatchAttendance(batchAttendanceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update attendance record
     */
    @PutMapping("/{attendanceId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<StudentAttendanceDTO> updateAttendance(
            @PathVariable Long attendanceId,
            @Valid @RequestBody StudentAttendanceDTO attendanceDTO) {
        log.info("Updating attendance record with id: {}", attendanceId);
        StudentAttendanceDTO response = attendanceService.updateAttendance(attendanceId, attendanceDTO);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all attendance for a student
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'STUDENT')")
    public ResponseEntity<List<StudentAttendanceDTO>> getAttendanceByUserId(@PathVariable String userId) {
        log.info("Fetching all attendance records for userId: {}", userId);
        List<StudentAttendanceDTO> attendance = attendanceService.getAttendanceByUserId(userId);
        return ResponseEntity.ok(attendance);
    }
    
    /**
     * Get attendance for a student in a date range
     */
    @GetMapping("/{userId}/range")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'STUDENT')")
    public ResponseEntity<List<StudentAttendanceDTO>> getAttendanceByDateRange(
            @PathVariable String userId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.info("Fetching attendance for userId: {} from {} to {}", userId, startDate, endDate);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<StudentAttendanceDTO> attendance = attendanceService.getAttendanceByUserIdAndDateRange(userId, start, end);
        return ResponseEntity.ok(attendance);
    }
    
    /**
     * Get attendance for a specific date (all students)
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentAttendanceDTO>> getAttendanceByDate(@PathVariable String date) {
        log.info("Fetching attendance for date: {}", date);
        LocalDate attendanceDate = LocalDate.parse(date);
        List<StudentAttendanceDTO> attendance = attendanceService.getAttendanceByDate(attendanceDate);
        return ResponseEntity.ok(attendance);
    }
    
    /**
     * Get attendance percentage for a student
     */
    @GetMapping("/{userId}/percentage")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> getAttendancePercentage(
            @PathVariable String userId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.info("Calculating attendance percentage for userId: {} from {} to {}", userId, startDate, endDate);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        double percentage = attendanceService.getAttendancePercentage(userId, start, end);
        double presentUnits = attendanceService.getPresentAttendanceUnits(userId, start, end);
        double totalUnits = attendanceService.getTotalAttendanceUnits(userId, start, end);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("attendancePercentage", percentage);
        response.put("presentDays", presentUnits);
        response.put("presentUnits", presentUnits);
        response.put("totalUnits", totalUnits);
        response.put("startDate", start);
        response.put("endDate", end);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get attendance for a specific date and session
     */
    @GetMapping("/date/{date}/session/{session}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentAttendanceDTO>> getAttendanceByDateAndSession(
            @PathVariable String date,
            @PathVariable String session) {
        log.info("Fetching attendance for date: {} and session: {}", date, session);
        LocalDate attendanceDate = LocalDate.parse(date);
        com.jd.apvd.entity.Session sessionEnum = com.jd.apvd.entity.Session.valueOf(session);
        List<StudentAttendanceDTO> attendance = attendanceService.getAttendanceByDateAndSession(attendanceDate, sessionEnum);
        return ResponseEntity.ok(attendance);
    }
    
    /**
     * Get attendance for multiple students (for filtered view)
     */
    @PostMapping("/students")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentAttendanceDTO>> getAttendanceByStudents(@RequestBody List<String> userIds) {
        log.info("Fetching attendance for {} students", userIds.size());
        List<StudentAttendanceDTO> attendance = attendanceService.getAttendanceByUserIds(userIds);
        return ResponseEntity.ok(attendance);
    }
    
    /**
     * Delete attendance record
     */
    @DeleteMapping("/{attendanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAttendance(@PathVariable Long attendanceId) {
        log.info("Deleting attendance record with id: {}", attendanceId);
        attendanceService.deleteAttendance(attendanceId);
        return ResponseEntity.ok("Attendance record deleted successfully");
    }
}
