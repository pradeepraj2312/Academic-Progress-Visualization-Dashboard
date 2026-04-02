package com.jd.apvd.controller;

import com.jd.apvd.dto.StudentMarksDTO;
import com.jd.apvd.service.StudentMarksService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:*", "https://*.development.catalystappsail.in"})
public class StudentMarksController {
    
    private final StudentMarksService marksService;
    
    /**
     * Save or update student marks
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<StudentMarksDTO> saveOrUpdateMarks(@Valid @RequestBody StudentMarksDTO marksDTO) {
        log.info("Saving/Updating marks for userId: {} and semester: {}", marksDTO.getUserId(), marksDTO.getSemester());
        StudentMarksDTO response = marksService.saveOrUpdateMarks(marksDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get marks for a student and semester
     */
    @GetMapping("/{userId}/semester/{semester}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'STUDENT')")
    public ResponseEntity<StudentMarksDTO> getMarksByUserIdAndSemester(
            @PathVariable String userId,
            @PathVariable Integer semester) {
        log.info("Fetching marks for userId: {} and semester: {}", userId, semester);
        StudentMarksDTO marks = marksService.getMarksByUserIdAndSemester(userId, semester);
        return ResponseEntity.ok(marks);
    }
    
    /**
     * Get all marks for a student
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'STUDENT')")
    public ResponseEntity<List<StudentMarksDTO>> getMarksByUserId(@PathVariable String userId) {
        log.info("Fetching all marks for userId: {}", userId);
        List<StudentMarksDTO> marks = marksService.getMarksByUserId(userId);
        return ResponseEntity.ok(marks);
    }
    
    /**
     * Get marks for a semester (all students)
     */
    @GetMapping("/semester/{semester}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentMarksDTO>> getMarksBySemester(@PathVariable Integer semester) {
        log.info("Fetching marks for semester: {}", semester);
        List<StudentMarksDTO> marks = marksService.getMarksBySemester(semester);
        return ResponseEntity.ok(marks);
    }
    
    /**
     * Get marks by student email
     */
    @GetMapping("/email/{userEmail}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'STUDENT')")
    public ResponseEntity<StudentMarksDTO> getMarksByUserEmail(@PathVariable String userEmail) {
        log.info("Fetching marks for email: {}", userEmail);
        StudentMarksDTO marks = marksService.getMarksByUserEmail(userEmail);
        return ResponseEntity.ok(marks);
    }
    
    /**
     * Update individual subject mark
     */
    @PutMapping("/{userId}/semester/{semester}/subject/{subjectNumber}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<StudentMarksDTO> updateSubjectMark(
            @PathVariable String userId,
            @PathVariable Integer semester,
            @PathVariable Integer subjectNumber,
            @RequestParam Double marks) {
        log.info("Updating subject {} mark for userId: {} and semester: {}", subjectNumber, userId, semester);
        StudentMarksDTO response = marksService.updateSubjectMark(userId, semester, subjectNumber, marks);
        return ResponseEntity.ok(response);
    }

    /**
     * Calculate overall CGPA for a student
     */
    @GetMapping("/{userId}/cgpa")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN', 'STUDENT')")
    public ResponseEntity<Map<String, Object>> calculateCGPA(@PathVariable String userId) {
        log.info("Calculating CGPA for userId: {}", userId);
        Double cgpa = marksService.calculateCGPA(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "cgpa", cgpa
        ));
    }
    
    /**
     * Delete marks
     */
    @DeleteMapping("/{marksId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteMarks(@PathVariable Long marksId) {
        log.info("Deleting marks with id: {}", marksId);
        marksService.deleteMarks(marksId);
        return ResponseEntity.ok("Marks deleted successfully");
    }
}
