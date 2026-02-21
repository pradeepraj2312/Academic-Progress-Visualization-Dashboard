package com.jd.apvd.controller;

import com.jd.apvd.dto.CourseDTO;
import com.jd.apvd.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    /**
     * Admin adds a new course
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> addCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.addCourse(courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }
    
    /**
     * Faculty/Admin updates course details
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(courseId, courseDTO);
        return ResponseEntity.ok(updatedCourse);
    }
    
    /**
     * Get course by ID (any authenticated user)
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long courseId) {
        CourseDTO course = courseService.getCourseById(courseId);
        return ResponseEntity.ok(course);
    }
    
    /**
     * Get course by course code
     */
    @GetMapping("/code/{courseCode}")
    public ResponseEntity<CourseDTO> getCourseByCourseCode(@PathVariable String courseCode) {
        CourseDTO course = courseService.getCourseByCourseCode(courseCode);
        return ResponseEntity.ok(course);
    }
    
    /**
     * Get all courses by department and semester
     */
    @GetMapping("/department/{department}/semester/{semester}")
    public ResponseEntity<List<CourseDTO>> getCoursesByDepartmentAndSemester(
            @PathVariable String department,
            @PathVariable Integer semester) {
        List<CourseDTO> courses = courseService.getCoursesByDepartmentAndSemester(department, semester);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Get core courses by department and semester
     */
    @GetMapping("/department/{department}/semester/{semester}/core")
    public ResponseEntity<List<CourseDTO>> getCoreCourses(
            @PathVariable String department,
            @PathVariable Integer semester) {
        List<CourseDTO> courses = courseService.getCoreCoursesByDepartmentAndSemester(department, semester);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Get elective courses by department and semester
     */
    @GetMapping("/department/{department}/semester/{semester}/elective")
    public ResponseEntity<List<CourseDTO>> getElectiveCourses(
            @PathVariable String department,
            @PathVariable Integer semester) {
        List<CourseDTO> courses = courseService.getElectiveCoursesByDepartmentAndSemester(department, semester);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Get all courses by faculty
     */
    @GetMapping("/faculty/{facultyUserId}")
    @PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
    public ResponseEntity<List<CourseDTO>> getCoursesByFaculty(
            @PathVariable String facultyUserId) {
        List<CourseDTO> courses = courseService.getCoursesByFaculty(facultyUserId);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Get all courses by department
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<CourseDTO>> getCoursesByDepartment(
            @PathVariable String department) {
        List<CourseDTO> courses = courseService.getCoursesByDepartment(department);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Get all courses by semester
     */
    @GetMapping("/semester/{semester}")
    public ResponseEntity<List<CourseDTO>> getCoursesBySemester(
            @PathVariable Integer semester) {
        List<CourseDTO> courses = courseService.getCoursesBySemester(semester);
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Delete course (Admin only)
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Check course availability
     */
    @GetMapping("/{courseId}/available")
    public ResponseEntity<Boolean> isCourseAvailable(@PathVariable Long courseId) {
        boolean available = courseService.isCourseAvailable(courseId);
        return ResponseEntity.ok(available);
    }
}
