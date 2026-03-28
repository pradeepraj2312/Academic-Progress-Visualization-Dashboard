package com.jd.apvd.service;

import com.jd.apvd.dto.StudentAttendanceDTO;
import com.jd.apvd.dto.BatchAttendanceDTO;
import com.jd.apvd.entity.Student;
import com.jd.apvd.entity.StudentAttendance;
import com.jd.apvd.repository.StudentAttendanceRepository;
import com.jd.apvd.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAttendanceService {
    
    private final StudentAttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    
    /**
     * Mark attendance for a student
     */
    @Transactional
    public StudentAttendanceDTO markAttendance(StudentAttendanceDTO attendanceDTO) {
        // Verify student exists
        Student student = studentRepository.findByUserId(attendanceDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Student not found with userId: " + attendanceDTO.getUserId()));
        
        // Create or update attendance record per user+date+session
        Optional<StudentAttendance> existingAttendance = attendanceRepository
            .findByUserIdAndAttendanceDateAndSession(
                student.getUserId(),
                attendanceDTO.getAttendanceDate(),
                attendanceDTO.getSession()
            );

        StudentAttendance attendance = existingAttendance.orElseGet(StudentAttendance::new);
        attendance.setUserId(student.getUserId());
        attendance.setUsername(student.getUsername());
        attendance.setUserEmail(student.getUserEmail());
        attendance.setAttendanceDate(attendanceDTO.getAttendanceDate());
        attendance.setSession(attendanceDTO.getSession());
        attendance.setStatus(attendanceDTO.getStatus());
        attendance.setRemarks(attendanceDTO.getRemarks());
        
        StudentAttendance savedAttendance = attendanceRepository.save(attendance);
        
        return mapAttendanceToDTO(savedAttendance, student);
    }
    
    /**
     * Update attendance for a specific date
     */
    @Transactional
    public StudentAttendanceDTO updateAttendance(Long attendanceId, StudentAttendanceDTO attendanceDTO) {
        StudentAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found with id: " + attendanceId));
        
        attendance.setStatus(attendanceDTO.getStatus());
        attendance.setSession(attendanceDTO.getSession());
        attendance.setRemarks(attendanceDTO.getRemarks());
        
        StudentAttendance updatedAttendance = attendanceRepository.save(attendance);
        
        Student student = studentRepository.findByUserId(attendance.getUserId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        return mapAttendanceToDTO(updatedAttendance, student);
    }
    
    /**
     * Get all attendance records for a student
     */
    public List<StudentAttendanceDTO> getAttendanceByUserId(String userId) {
        List<StudentAttendance> attendanceList = attendanceRepository.findByUserIdOrderByAttendanceDateDesc(userId);
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return attendanceList.stream()
                .map(a -> mapAttendanceToDTO(a, student))
                .collect(Collectors.toList());
    }
    
    /**
     * Get attendance records for a student between two dates
     */
    public List<StudentAttendanceDTO> getAttendanceByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        List<StudentAttendance> attendanceList = attendanceRepository
                .findByUserIdAndAttendanceDateBetween(userId, startDate, endDate);
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return attendanceList.stream()
                .map(a -> mapAttendanceToDTO(a, student))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all attendance for a specific date
     */
    public List<StudentAttendanceDTO> getAttendanceByDate(LocalDate date) {
        List<StudentAttendance> attendanceList = attendanceRepository.findByAttendanceDate(date);
        return attendanceList.stream()
                .map(attendance -> {
                    Student student = studentRepository.findByUserId(attendance.getUserId())
                            .orElse(null);
                    return mapAttendanceToDTO(attendance, student);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get attendance count for a student in a date range
     */
    public long getAttendanceCount(String userId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startDate, endDate)
                .stream()
                .filter(a -> a.getStatus() == com.jd.apvd.entity.AttendanceStatus.PRESENT)
                .count();
    }

    public double getPresentAttendanceUnits(String userId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startDate, endDate)
                .stream()
                .filter(a -> a.getStatus() == com.jd.apvd.entity.AttendanceStatus.PRESENT)
                .mapToDouble(a -> 0.5)
                .sum();
    }

    public double getTotalAttendanceUnits(String userId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startDate, endDate)
                .stream()
                .mapToDouble(a -> 0.5)
                .sum();
    }
    
    /**
     * Get attendance percentage for a student in a date range
     */
    public double getAttendancePercentage(String userId, LocalDate startDate, LocalDate endDate) {
        double presentUnits = getPresentAttendanceUnits(userId, startDate, endDate);
        double totalUnits = getTotalAttendanceUnits(userId, startDate, endDate);

        if (totalUnits == 0) {
            return 0;
        }

        return (presentUnits * 100.0) / totalUnits;
    }
    
    /**
     * Mark attendance for multiple students at once
     */
    @Transactional
    public List<StudentAttendanceDTO> markBatchAttendance(BatchAttendanceDTO batchDTO) {
        return batchDTO.getStudentIds().stream()
                .map(studentId -> {
                    Student student = studentRepository.findByUserId(studentId)
                            .orElseThrow(() -> new RuntimeException("Student not found with userId: " + studentId));
                    
                    // Check for duplicate attendance
                    Optional<StudentAttendance> existingAttendance = attendanceRepository
                            .findByUserIdAndAttendanceDateAndSession(studentId, batchDTO.getAttendanceDate(), batchDTO.getSession());
                    
                    StudentAttendance attendance;
                    if (existingAttendance.isPresent()) {
                        // Update existing record
                        attendance = existingAttendance.get();
                        attendance.setStatus(batchDTO.getStatus());
                        attendance.setRemarks(batchDTO.getRemarks());
                    } else {
                        // Create new record
                        attendance = new StudentAttendance();
                        attendance.setUserId(student.getUserId());
                        attendance.setUsername(student.getUsername());
                        attendance.setUserEmail(student.getUserEmail());
                        attendance.setAttendanceDate(batchDTO.getAttendanceDate());
                        attendance.setSession(batchDTO.getSession());
                        attendance.setStatus(batchDTO.getStatus());
                        attendance.setRemarks(batchDTO.getRemarks());
                    }
                    
                    StudentAttendance savedAttendance = attendanceRepository.save(attendance);
                    return mapAttendanceToDTO(savedAttendance, student);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get attendance records for a specific date and session
     */
    public List<StudentAttendanceDTO> getAttendanceByDateAndSession(LocalDate date, com.jd.apvd.entity.Session session) {
        List<StudentAttendance> attendanceList = attendanceRepository.findByAttendanceDateAndSession(date, session);
        return attendanceList.stream()
                .map(attendance -> {
                    Student student = studentRepository.findByUserId(attendance.getUserId())
                            .orElse(null);
                    return mapAttendanceToDTO(attendance, student);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get attendance records for multiple students
     */
    public List<StudentAttendanceDTO> getAttendanceByUserIds(List<String> userIds) {
        List<StudentAttendance> attendanceList = attendanceRepository.findByUserIdIn(userIds);
        Map<String, Student> studentsByUserId = studentRepository.findByUserIdIn(userIds).stream()
            .collect(Collectors.toMap(Student::getUserId, Function.identity()));

        return attendanceList.stream()
            .map(attendance -> mapAttendanceToDTO(attendance, studentsByUserId.get(attendance.getUserId())))
                .collect(Collectors.toList());
    }
    
    /**
     * Delete attendance record
     */
    @Transactional
    public void deleteAttendance(Long attendanceId) {
        StudentAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found with id: " + attendanceId));
        attendanceRepository.delete(attendance);
    }
    
    private StudentAttendanceDTO mapAttendanceToDTO(StudentAttendance attendance, Student student) {
        StudentAttendanceDTO dto = new StudentAttendanceDTO();
        dto.setId(attendance.getId());
        dto.setUserId(attendance.getUserId());
        dto.setUsername(attendance.getUsername());
        dto.setUserEmail(attendance.getUserEmail());
        if (student != null) {
            dto.setEnrollmentNumber(student.getEnrollmentNumber());
            dto.setDepartment(student.getDepartment());
            dto.setYearOfStudying(student.getYearOfStudying());
        }
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setSession(attendance.getSession());
        dto.setStatus(attendance.getStatus());
        dto.setRemarks(attendance.getRemarks());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());
        return dto;
    }
}
