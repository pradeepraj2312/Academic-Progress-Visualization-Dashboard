package com.jd.apvd.controller;

import com.jd.apvd.dto.GradeDTO;
import com.jd.apvd.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {
    
    private final GradeService gradeService;
    
    /**
     * Faculty assigns grade to a student for a course
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<GradeDTO> assignGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        GradeDTO assignedGrade = gradeService.assignGrade(gradeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignedGrade);
    }
    
    /**
     * Faculty updates an existing grade
     */
    @PutMapping("/{gradeId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<GradeDTO> updateGrade(
            @PathVariable Long gradeId,
            @Valid @RequestBody GradeDTO gradeDTO) {
        GradeDTO updatedGrade = gradeService.updateGrade(gradeId, gradeDTO);
        return ResponseEntity.ok(updatedGrade);
    }
    
    /**
     * Get all grades for a student
     */
    @GetMapping("/student/{studentUserId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<GradeDTO>> getGradesByStudent(@PathVariable String studentUserId) {
        List<GradeDTO> grades = gradeService.getGradesByStudent(studentUserId);
        return ResponseEntity.ok(grades);
    }
    
    /**
     * Get grades for a student in a specific semester
     */
    @GetMapping("/student/{studentUserId}/semester/{semester}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentAndSemester(
            @PathVariable String studentUserId,
            @PathVariable Integer semester) {
        List<GradeDTO> grades = gradeService.getGradesByStudentAndSemester(studentUserId, semester);
        return ResponseEntity.ok(grades);
    }
    
    /**
     * Get all grades for a specific course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<GradeDTO>> getGradesByCourse(@PathVariable Long courseId) {
        List<GradeDTO> grades = gradeService.getGradesByCourse(courseId);
        return ResponseEntity.ok(grades);
    }
    
    /**
     * Get specific grade for student-course-semester combination
     */
    @GetMapping("/student/{studentUserId}/course/{courseId}/semester/{semester}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<GradeDTO> getGradeByStudentAndCourse(
            @PathVariable String studentUserId,
            @PathVariable Long courseId,
            @PathVariable Integer semester) {
        GradeDTO grade = gradeService.getGradeByStudentAndCourse(studentUserId, courseId, semester);
        return ResponseEntity.ok(grade);
    }
    
    /**
     * Calculate SGPA for a student in a semester
     */
    @GetMapping("/student/{studentUserId}/semester/{semester}/sgpa")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> calculateSGPA(
            @PathVariable String studentUserId,
            @PathVariable Integer semester) {
        Double sgpa = gradeService.calculateSGPA(studentUserId, semester);
        return ResponseEntity.ok(Map.of(
                "studentUserId", studentUserId,
                "semester", semester,
                "sgpa", sgpa
        ));
    }
    
    /**
     * Calculate CGPA for a student
     */
    @GetMapping("/student/{studentUserId}/cgpa")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> calculateCGPA(@PathVariable String studentUserId) {
        Double cgpa = gradeService.calculateCGPA(studentUserId);
        return ResponseEntity.ok(Map.of(
                "studentUserId", studentUserId,
                "cgpa", cgpa
        ));
    }
    
    /**
     * Get comprehensive grade report for a student (all semesters)
     */
    @GetMapping("/student/{studentUserId}/report")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getStudentGradeReport(@PathVariable String studentUserId) {
        Map<String, Object> report = gradeService.getStudentGradeReport(studentUserId);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get semester grade report for a student
     */
    @GetMapping("/student/{studentUserId}/semester/{semester}/report")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getSemesterGradeReport(
            @PathVariable String studentUserId,
            @PathVariable Integer semester) {
        Map<String, Object> report = gradeService.getSemesterGradeReport(studentUserId, semester);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get grade statistics for a course (pass/fail rates, average, etc.)
     */
    @GetMapping("/course/{courseId}/statistics")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourseGradeStatistics(@PathVariable Long courseId) {
        Map<String, Object> stats = gradeService.getCourseGradeStatistics(courseId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Delete grade (Admin only)
     */
    @DeleteMapping("/{gradeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long gradeId) {
        gradeService.deleteGrade(gradeId);
        return ResponseEntity.noContent().build();
    }
}
