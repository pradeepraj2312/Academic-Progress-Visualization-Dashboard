package com.jd.apvd.controller;

import com.jd.apvd.dto.StudentCourseSelectionDTO;
import com.jd.apvd.service.StudentCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student-courses")
@RequiredArgsConstructor
public class StudentCourseController {
    
    private final StudentCourseService studentCourseService;
    
    /**
     * Student selects a course (4 core courses max + 2 elective courses max per semester)
     */
    @PostMapping("/select")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentCourseSelectionDTO> selectCourse(
            @Valid @RequestBody StudentCourseSelectionDTO selectionDTO) {
        StudentCourseSelectionDTO selectedCourse = studentCourseService.selectCourse(selectionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(selectedCourse);
    }
    
    /**
     * Get all courses selected by a student for a specific semester
     */
    @GetMapping("/{studentUserId}/semester/{semester}")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentCourseSelectionDTO>> getStudentCoursesBySemester(
            @PathVariable String studentUserId,
            @PathVariable Integer semester) {
        List<StudentCourseSelectionDTO> courses = studentCourseService.getStudentCoursesBySemester(studentUserId, semester);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Get all core courses selected by a student
     */
    @GetMapping("/{studentUserId}/semester/{semester}/core")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentCourseSelectionDTO>> getStudentCoreCourses(
            @PathVariable String studentUserId,
            @PathVariable Integer semester) {
        List<StudentCourseSelectionDTO> courses = studentCourseService.getStudentCoreCourses(studentUserId, semester);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Get all elective courses selected by a student
     */
    @GetMapping("/{studentUserId}/semester/{semester}/elective")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<List<StudentCourseSelectionDTO>> getStudentElectiveCourses(
            @PathVariable String studentUserId,
            @PathVariable Integer semester) {
        List<StudentCourseSelectionDTO> courses = studentCourseService.getStudentElectiveCourses(studentUserId, semester);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Drop a course (student removes enrollment)
     */
    @DeleteMapping("/{studentUserId}/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> dropCourse(
            @PathVariable String studentUserId,
            @PathVariable Long courseId) {
        studentCourseService.dropCourse(studentUserId, courseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Faculty/Admin edits a student's selected course
     */
    @PutMapping("/{studentUserId}/course/{oldCourseId}/replace/{newCourseId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<StudentCourseSelectionDTO> replaceStudentCourse(
            @PathVariable String studentUserId,
            @PathVariable Long oldCourseId,
            @PathVariable Long newCourseId,
            @RequestParam("semester") Integer semester) {
        StudentCourseSelectionDTO updated = studentCourseService.replaceStudentCourse(
                studentUserId, oldCourseId, newCourseId, semester);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Get course selection status - shows how many courses selected vs max allowed
     */
    @GetMapping("/{studentUserId}/semester/{semester}/status")
    @PreAuthorize("hasAnyRole('STUDENT', 'FACULTY', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourseSelectionStatus(
            @PathVariable String studentUserId,
            @PathVariable Integer semester) {
        StudentCourseService.StudentCourseSelectionStatus status = 
                studentCourseService.getCourseSelectionStatus(studentUserId, semester);
        
        Map<String, Object> response = new HashMap<>();
        response.put("studentUserId", status.studentUserId);
        response.put("semester", status.semester);
        response.put("coreCoursesSelected", status.coreCoursesSelected);
        response.put("coreElectiveCoursesSelected", status.coreElectiveCoursesSelected);
        response.put("openElectiveCoursesSelected", status.openElectiveCoursesSelected);
        response.put("maxCoreAllowed", status.maxCoreAllowed);
        response.put("maxCoreElectiveAllowed", status.maxCoreElectiveAllowed);
        response.put("maxOpenElectiveAllowed", status.maxOpenElectiveAllowed);
        response.put("coreCoursesRemaining", status.maxCoreAllowed - status.coreCoursesSelected);
        response.put("coreElectiveCoursesRemaining", status.maxCoreElectiveAllowed - status.coreElectiveCoursesSelected);
        response.put("openElectiveCoursesRemaining", status.maxOpenElectiveAllowed - status.openElectiveCoursesSelected);
        response.put("totalCoursesSelected", status.coreCoursesSelected + status.coreElectiveCoursesSelected + status.openElectiveCoursesSelected);
        response.put("totalCoursesAllowed", status.maxCoreAllowed + status.maxCoreElectiveAllowed + status.maxOpenElectiveAllowed);
        
        return ResponseEntity.ok(response);
    }
}
