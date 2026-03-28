package com.jd.apvd.controller;

import com.jd.apvd.dto.CourseDTO;
import com.jd.apvd.dto.BulkUploadResultDTO;
import com.jd.apvd.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
     * Admin uploads courses from Excel
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResultDTO> uploadCourses(@RequestParam("file") MultipartFile file) {
        BulkUploadResultDTO result = courseService.bulkUploadCourses(file);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all courses
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
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
     * Reassign all courses from one faculty to another (Admin only)
     */
    @PutMapping("/reassign-faculty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reassignFacultyCourses(
            @RequestParam String fromFacultyUserId,
            @RequestParam String toFacultyUserId) {
        int reassignedCount = courseService.reassignCoursesToFaculty(fromFacultyUserId, toFacultyUserId);
        return ResponseEntity.ok(Map.of(
                "message", "Courses reassigned successfully",
                "reassignedCount", reassignedCount,
                "fromFacultyUserId", fromFacultyUserId,
                "toFacultyUserId", toFacultyUserId
        ));
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
