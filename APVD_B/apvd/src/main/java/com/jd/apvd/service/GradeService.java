package com.jd.apvd.service;

import com.jd.apvd.dto.GradeDTO;
import com.jd.apvd.entity.Course;
import com.jd.apvd.entity.Grade;
import com.jd.apvd.entity.Student;
import com.jd.apvd.repository.CourseRepository;
import com.jd.apvd.repository.GradeRepository;
import com.jd.apvd.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeService {
    
    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    
    /**
     * Faculty assigns grade to a student for a course
     */
    @Transactional
    public GradeDTO assignGrade(GradeDTO gradeDTO) {
        // Verify student exists
        Student student = studentRepository.findByUserId(gradeDTO.getStudentUserId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Verify course exists
        Course course = courseRepository.findById(gradeDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Check if grade already exists for this student-course-semester combination
        Grade grade = gradeRepository.findByStudentUserIdAndCourseIdAndSemester(
                gradeDTO.getStudentUserId(), gradeDTO.getCourseId(), gradeDTO.getSemester())
                .orElse(new Grade());
        
        // Set grade properties
        grade.setStudentUserId(gradeDTO.getStudentUserId());
        grade.setCourseId(gradeDTO.getCourseId());
        grade.setSemester(gradeDTO.getSemester());
        grade.setDepartment(gradeDTO.getDepartment() != null ? gradeDTO.getDepartment() : student.getDepartment());
        grade.setCourseCode(course.getCourseCode());
        grade.setCourseName(course.getCourseName());
        grade.setCourseStatus(course.getCourseStatus());
        grade.setMarks(gradeDTO.getMarks());
        grade.setCredits(course.getCredits());
        grade.setGradedByFacultyId(gradeDTO.getGradedByFacultyId());
        grade.setRemarks(gradeDTO.getRemarks());
        
        // Grade calculation happens automatically via @PrePersist/@PreUpdate
        Grade savedGrade = gradeRepository.save(grade);
        
        log.info("Grade assigned for student {} in course {} ({}): {} marks = {} grade",
                gradeDTO.getStudentUserId(), course.getCourseCode(), course.getCourseName(),
                savedGrade.getMarks(), savedGrade.getLetterGrade());
        
        return mapGradeToDTO(savedGrade);
    }
    
    /**
     * Update grade for a student-course combination
     */
    @Transactional
    public GradeDTO updateGrade(Long gradeId, GradeDTO gradeDTO) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
        
        grade.setMarks(gradeDTO.getMarks());
        grade.setRemarks(gradeDTO.getRemarks());
        grade.setGradedByFacultyId(gradeDTO.getGradedByFacultyId());
        
        // Grade will be recalculated automatically via @PreUpdate
        Grade updatedGrade = gradeRepository.save(grade);
        
        log.info("Grade updated: ID {} - {} marks = {} grade", 
                gradeId, updatedGrade.getMarks(), updatedGrade.getLetterGrade());
        
        return mapGradeToDTO(updatedGrade);
    }
    
    /**
     * Get all grades for a student
     */
    public List<GradeDTO> getGradesByStudent(String studentUserId) {
        return gradeRepository.findByStudentUserId(studentUserId)
                .stream()
                .map(this::mapGradeToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get grades for a student in a specific semester
     */
    public List<GradeDTO> getGradesByStudentAndSemester(String studentUserId, Integer semester) {
        return gradeRepository.findByStudentUserIdAndSemester(studentUserId, semester)
                .stream()
                .map(this::mapGradeToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all grades for a specific course
     */
    public List<GradeDTO> getGradesByCourse(Long courseId) {
        return gradeRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapGradeToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get single grade for student-course-semester
     */
    public GradeDTO getGradeByStudentAndCourse(String studentUserId, Long courseId, Integer semester) {
        Grade grade = gradeRepository.findByStudentUserIdAndCourseIdAndSemester(studentUserId, courseId, semester)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
        return mapGradeToDTO(grade);
    }
    
    /**
     * Calculate SGPA (Semester Grade Point Average) for a student in a semester
     */
    public Double calculateSGPA(String studentUserId, Integer semester) {
        Double sgpa = gradeRepository.calculateSGPAForSemester(studentUserId, semester);
        
        if (sgpa == null) {
            return 0.0;
        }
        
        // Round to 2 decimal places
        return BigDecimal.valueOf(sgpa)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    
    /**
     * Calculate CGPA (Cumulative Grade Point Average) for a student
     */
    public Double calculateCGPA(String studentUserId) {
        Double cgpa = gradeRepository.calculateCGPA(studentUserId);
        
        if (cgpa == null) {
            return 0.0;
        }
        
        // Round to 2 decimal places
        return BigDecimal.valueOf(cgpa)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    
    /**
     * Get comprehensive grade report for a student
     */
    public Map<String, Object> getStudentGradeReport(String studentUserId) {
        Map<String, Object> report = new HashMap<>();
        
        // Get all grades
        List<Grade> grades = gradeRepository.findByStudentUserId(studentUserId);
        
        // Calculate CGPA
        Double cgpa = calculateCGPA(studentUserId);
        
        // Count failed courses
        Long failedCourses = gradeRepository.countFailedCourses(studentUserId);
        
        // Calculate semester-wise SGPA
        Map<Integer, Double> semesterSGPAs = new HashMap<>();
        Map<Integer, List<GradeDTO>> semesterGrades = new HashMap<>();
        
        for (Grade grade : grades) {
            Integer semester = grade.getSemester();
            if (!semesterSGPAs.containsKey(semester)) {
                semesterSGPAs.put(semester, calculateSGPA(studentUserId, semester));
            }
            
            if (!semesterGrades.containsKey(semester)) {
                semesterGrades.put(semester, getGradesByStudentAndSemester(studentUserId, semester));
            }
        }
        
        report.put("studentUserId", studentUserId);
        report.put("cgpa", cgpa);
        report.put("totalCourses", grades.size());
        report.put("failedCourses", failedCourses);
        report.put("passedCourses", grades.size() - failedCourses);
        report.put("semesterSGPAs", semesterSGPAs);
        report.put("semesterGrades", semesterGrades);
        report.put("allGrades", grades.stream().map(this::mapGradeToDTO).collect(Collectors.toList()));
        
        return report;
    }
    
    /**
     * Get semester grade report for a student
     */
    public Map<String, Object> getSemesterGradeReport(String studentUserId, Integer semester) {
        Map<String, Object> report = new HashMap<>();
        
        List<GradeDTO> grades = getGradesByStudentAndSemester(studentUserId, semester);
        Double sgpa = calculateSGPA(studentUserId, semester);
        
        long failedCount = grades.stream()
                .filter(g -> "F".equals(g.getLetterGrade()))
                .count();
        
        report.put("studentUserId", studentUserId);
        report.put("semester", semester);
        report.put("sgpa", sgpa);
        report.put("totalCourses", grades.size());
        report.put("failedCourses", failedCount);
        report.put("passedCourses", grades.size() - failedCount);
        report.put("grades", grades);
        
        return report;
    }
    
    /**
     * Delete grade (Admin only)
     */
    @Transactional
    public void deleteGrade(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
        
        gradeRepository.delete(grade);
        log.info("Grade deleted: ID {}", gradeId);
    }
    
    /**
     * Get grade statistics for a course
     */
    public Map<String, Object> getCourseGradeStatistics(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        
        if (grades.isEmpty()) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("courseId", courseId);
            emptyStats.put("totalStudents", 0);
            emptyStats.put("message", "No grades available");
            return emptyStats;
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        double averageMarks = grades.stream()
                .mapToDouble(Grade::getMarks)
                .average()
                .orElse(0.0);
        
        double highestMarks = grades.stream()
                .mapToDouble(Grade::getMarks)
                .max()
                .orElse(0.0);
        
        double lowestMarks = grades.stream()
                .mapToDouble(Grade::getMarks)
                .min()
                .orElse(0.0);
        
        long passedCount = grades.stream()
                .filter(g -> !"F".equals(g.getLetterGrade()))
                .count();
        
        long failedCount = grades.size() - passedCount;
        
        double passPercentage = (passedCount * 100.0) / grades.size();
        
        stats.put("courseId", courseId);
        stats.put("courseCode", grades.get(0).getCourseCode());
        stats.put("courseName", grades.get(0).getCourseName());
        stats.put("totalStudents", grades.size());
        stats.put("averageMarks", BigDecimal.valueOf(averageMarks).setScale(2, RoundingMode.HALF_UP).doubleValue());
        stats.put("highestMarks", highestMarks);
        stats.put("lowestMarks", lowestMarks);
        stats.put("passedStudents", passedCount);
        stats.put("failedStudents", failedCount);
        stats.put("passPercentage", BigDecimal.valueOf(passPercentage).setScale(2, RoundingMode.HALF_UP).doubleValue());
        
        return stats;
    }
    
    private GradeDTO mapGradeToDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setGradeId(grade.getGradeId());
        dto.setStudentUserId(grade.getStudentUserId());
        dto.setCourseId(grade.getCourseId());
        dto.setSemester(grade.getSemester());
        dto.setDepartment(grade.getDepartment());
        dto.setCourseCode(grade.getCourseCode());
        dto.setCourseName(grade.getCourseName());
        dto.setCourseStatus(grade.getCourseStatus());
        dto.setMarks(grade.getMarks());
        dto.setLetterGrade(grade.getLetterGrade());
        dto.setGradePoint(grade.getGradePoint());
        dto.setCredits(grade.getCredits());
        dto.setGradedByFacultyId(grade.getGradedByFacultyId());
        dto.setRemarks(grade.getRemarks());
        return dto;
    }
}

