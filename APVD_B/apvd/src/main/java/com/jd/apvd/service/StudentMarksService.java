package com.jd.apvd.service;

import com.jd.apvd.dto.StudentMarksDTO;
import com.jd.apvd.entity.Course;
import com.jd.apvd.entity.Student;
import com.jd.apvd.entity.StudentCourse;
import com.jd.apvd.entity.StudentMarks;
import com.jd.apvd.repository.CourseRepository;
import com.jd.apvd.repository.StudentMarksRepository;
import com.jd.apvd.repository.StudentCourseRepository;
import com.jd.apvd.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentMarksService {
    
    private final StudentMarksRepository studentMarksRepository;
    private final StudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    
    /**
     * Add or update student marks for a semester
     */
    @Transactional
    public StudentMarksDTO saveOrUpdateMarks(StudentMarksDTO marksDTO) {
        // Verify student exists
        Student student = studentRepository.findByUserId(marksDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Student not found with userId: " + marksDTO.getUserId()));
        
        // Check if marks already exist for this semester
        StudentMarks marks = studentMarksRepository
                .findByUserIdAndSemester(marksDTO.getUserId(), marksDTO.getSemester())
                .orElse(new StudentMarks());
        
        // Update marks
        marks.setUserId(student.getUserId());
        marks.setUsername(student.getUsername());
        marks.setUserEmail(student.getUserEmail());
        marks.setSemester(marksDTO.getSemester());
        marks.setSubject1Mark(marksDTO.getSubject1Mark());
        marks.setSubject2Mark(marksDTO.getSubject2Mark());
        marks.setSubject3Mark(marksDTO.getSubject3Mark());
        marks.setSubject4Mark(marksDTO.getSubject4Mark());
        marks.setSubject5Mark(marksDTO.getSubject5Mark());
        marks.setSubject6Mark(marksDTO.getSubject6Mark());
        applySelectedSubjectNames(marks, marksDTO.getUserId(), marksDTO.getSemester());
        
        StudentMarks savedMarks = studentMarksRepository.save(marks);
        
        return mapMarksToDTO(savedMarks, student);
    }
    
    /**
     * Get marks for a specific student and semester
     */
    public StudentMarksDTO getMarksByUserIdAndSemester(String userId, Integer semester) {
        return studentMarksRepository
                .findByUserIdAndSemester(userId, semester)
                .map(marks -> {
                    Student student = studentRepository.findByUserId(marks.getUserId()).orElse(null);
                    return mapMarksToDTO(marks, student);
                })
                .orElseGet(() -> {
                    // Return empty marks object if not found
                    StudentMarksDTO dto = new StudentMarksDTO();
                    dto.setUserId(userId);
                    dto.setSemester(semester);
                    dto.setSubject1Mark(0.0);
                    dto.setSubject2Mark(0.0);
                    dto.setSubject3Mark(0.0);
                    dto.setSubject4Mark(0.0);
                    dto.setSubject5Mark(0.0);
                    dto.setSubject6Mark(0.0);
                    dto.setSubject1Name("Subject 1");
                    dto.setSubject2Name("Subject 2");
                    dto.setSubject3Name("Subject 3");
                    dto.setSubject4Name("Subject 4");
                    dto.setSubject5Name("Subject 5");
                    dto.setSubject6Name("Subject 6");
                    dto.setTotalMarks(0.0);
                    dto.setSgpa(0.0);
                    return dto;
                });
    }
    
    /**
     * Get all marks for a student
     */
    public List<StudentMarksDTO> getMarksByUserId(String userId) {
        List<StudentMarks> marksList = studentMarksRepository.findByUserId(userId);
        Student student = studentRepository.findByUserId(userId).orElse(null);
        return marksList.stream()
            .map(marks -> mapMarksToDTO(marks, student))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all marks for a semester
     */
    public List<StudentMarksDTO> getMarksBySemester(Integer semester) {
        List<StudentMarks> marksList = studentMarksRepository.findBySemester(semester);
        return marksList.stream()
                .map(marks -> {
                    Student student = studentRepository.findByUserId(marks.getUserId()).orElse(null);
                    return mapMarksToDTO(marks, student);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get marks by student email
     */
    public StudentMarksDTO getMarksByUserEmail(String userEmail) {
        StudentMarks marks = studentMarksRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Marks not found for email: " + userEmail));
        
        Student student = studentRepository.findByUserId(marks.getUserId()).orElse(null);
        return mapMarksToDTO(marks, student);
    }
    
    /**
     * Delete marks
     */
    @Transactional
    public void deleteMarks(Long marksId) {
        StudentMarks marks = studentMarksRepository.findById(marksId)
                .orElseThrow(() -> new RuntimeException("Marks not found with id: " + marksId));
        studentMarksRepository.delete(marks);
    }
    
    /**
     * Update individual subject marks
     */
    @Transactional
    public StudentMarksDTO updateSubjectMark(String userId, Integer semester, Integer subjectNumber, Double marks) {
        StudentMarks studentMarks = studentMarksRepository
                .findByUserIdAndSemester(userId, semester)
                .orElseThrow(() -> new RuntimeException("Marks not found"));
        
        switch (subjectNumber) {
            case 1:
                studentMarks.setSubject1Mark(marks);
                break;
            case 2:
                studentMarks.setSubject2Mark(marks);
                break;
            case 3:
                studentMarks.setSubject3Mark(marks);
                break;
            case 4:
                studentMarks.setSubject4Mark(marks);
                break;
            case 5:
                studentMarks.setSubject5Mark(marks);
                break;
            case 6:
                studentMarks.setSubject6Mark(marks);
                break;
            default:
                throw new RuntimeException("Invalid subject number: " + subjectNumber);
        }
        
        StudentMarks updatedMarks = studentMarksRepository.save(studentMarks);
        Student student = studentRepository.findByUserId(updatedMarks.getUserId()).orElse(null);
        return mapMarksToDTO(updatedMarks, student);
    }

    /**
     * Calculate overall CGPA for a student (average of all semester SGPA values)
     */
    public Double calculateCGPA(String userId) {
        Double cgpa = studentMarksRepository.calculateCGPA(userId);
        
        if (cgpa == null) {
            return 0.0;
        }
        
        // Round to 2 decimal places
        return BigDecimal.valueOf(cgpa)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    
    private StudentMarksDTO mapMarksToDTO(StudentMarks marks, Student student) {
        StudentMarksDTO dto = new StudentMarksDTO();
        dto.setId(marks.getId());
        dto.setUserId(marks.getUserId());
        dto.setUsername(marks.getUsername());
        dto.setUserEmail(marks.getUserEmail());
        if (student != null) {
            dto.setEnrollmentNumber(student.getEnrollmentNumber());
            dto.setDepartment(student.getDepartment());
            dto.setYearOfStudying(student.getYearOfStudying());
        }
        dto.setSemester(marks.getSemester());
        dto.setSubject1Mark(marks.getSubject1Mark());
        dto.setSubject2Mark(marks.getSubject2Mark());
        dto.setSubject3Mark(marks.getSubject3Mark());
        dto.setSubject4Mark(marks.getSubject4Mark());
        dto.setSubject5Mark(marks.getSubject5Mark());
        dto.setSubject6Mark(marks.getSubject6Mark());
        dto.setSubject1Name(marks.getSubject1Name());
        dto.setSubject2Name(marks.getSubject2Name());
        dto.setSubject3Name(marks.getSubject3Name());
        dto.setSubject4Name(marks.getSubject4Name());
        dto.setSubject5Name(marks.getSubject5Name());
        dto.setSubject6Name(marks.getSubject6Name());
        dto.setTotalMarks(marks.getTotalMarks());
        dto.setSgpa(marks.getSgpa());
        dto.setCreatedAt(marks.getCreatedAt());
        dto.setUpdatedAt(marks.getUpdatedAt());
        return dto;
    }

    private void applySelectedSubjectNames(StudentMarks marks, String userId, Integer semester) {
        List<StudentCourse> selectedCourses = studentCourseRepository.findByStudentUserIdAndSemester(userId, semester)
                .stream()
                .sorted(Comparator.comparing(StudentCourse::getCourseId))
                .limit(6)
                .collect(Collectors.toList());

        Map<Long, Course> coursesById = new HashMap<>();
        if (!selectedCourses.isEmpty()) {
            coursesById = courseRepository.findAllById(
                    selectedCourses.stream().map(StudentCourse::getCourseId).collect(Collectors.toList())
            ).stream().collect(Collectors.toMap(Course::getCourseId, course -> course));
        }

        setSubjectName(marks::setSubject1Name, selectedCourses, coursesById, 0, "Subject 1");
        setSubjectName(marks::setSubject2Name, selectedCourses, coursesById, 1, "Subject 2");
        setSubjectName(marks::setSubject3Name, selectedCourses, coursesById, 2, "Subject 3");
        setSubjectName(marks::setSubject4Name, selectedCourses, coursesById, 3, "Subject 4");
        setSubjectName(marks::setSubject5Name, selectedCourses, coursesById, 4, "Subject 5");
        setSubjectName(marks::setSubject6Name, selectedCourses, coursesById, 5, "Subject 6");
    }

    private void setSubjectName(Consumer<String> setter,
                                List<StudentCourse> selectedCourses,
                                Map<Long, Course> coursesById,
                                int index,
                                String fallback) {
        if (selectedCourses.size() > index) {
            Course course = coursesById.get(selectedCourses.get(index).getCourseId());
            if (course != null && course.getCourseName() != null && !course.getCourseName().isBlank()) {
                setter.accept(course.getCourseName());
                return;
            }
        }
        setter.accept(fallback);
    }
}
