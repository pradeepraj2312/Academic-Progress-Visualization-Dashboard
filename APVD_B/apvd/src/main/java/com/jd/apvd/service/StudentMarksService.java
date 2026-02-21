package com.jd.apvd.service;

import com.jd.apvd.dto.StudentMarksDTO;
import com.jd.apvd.entity.Student;
import com.jd.apvd.entity.StudentMarks;
import com.jd.apvd.repository.StudentMarksRepository;
import com.jd.apvd.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentMarksService {
    
    private final StudentMarksRepository studentMarksRepository;
    private final StudentRepository studentRepository;
    
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
        dto.setTotalMarks(marks.getTotalMarks());
        dto.setSgpa(marks.getSgpa());
        dto.setCreatedAt(marks.getCreatedAt());
        dto.setUpdatedAt(marks.getUpdatedAt());
        return dto;
    }
}
