package com.jd.apvd.service;

import com.jd.apvd.dto.StudentCourseSelectionDTO;
import com.jd.apvd.entity.Course;
import com.jd.apvd.entity.CourseStatus;
import com.jd.apvd.entity.Student;
import com.jd.apvd.entity.StudentCourse;
import com.jd.apvd.repository.CourseRepository;
import com.jd.apvd.repository.StudentCourseRepository;
import com.jd.apvd.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentCourseService {
    
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final CourseService courseService;
    
    private static final int MAX_CORE_COURSES = 4;
    private static final int MAX_CORE_ELECTIVE_COURSES = 2;
    private static final int MAX_OPEN_ELECTIVE_COURSES = 1;
    
    /**
     * Student selects a course
     * Validates: max 4 core courses and 2 elective courses per semester
     */
    @Transactional
    public StudentCourseSelectionDTO selectCourse(StudentCourseSelectionDTO selectionDTO) {
        // Verify student exists
        Student student = studentRepository.findByUserId(selectionDTO.getStudentUserId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Verify course exists
        Course course = courseRepository.findById(selectionDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Course and selection semester must match
        if (!course.getSemester().equals(selectionDTO.getSemester())) {
            throw new RuntimeException("Course semester does not match selected semester");
        }
        
        // Check if course is already selected by this student
        if (studentCourseRepository.findByStudentUserIdAndCourseId(
                selectionDTO.getStudentUserId(), selectionDTO.getCourseId()).isPresent()) {
            throw new RuntimeException("Course already selected by this student");
        }

        validateDepartmentAndSemesterRules(student, course, selectionDTO.getSemester());
        validateSelectionLimits(selectionDTO.getStudentUserId(), selectionDTO.getSemester(), course.getCourseStatus(), null);
        
        // Check if course capacity is available
        if (!courseService.isCourseAvailable(selectionDTO.getCourseId())) {
            throw new RuntimeException("Course capacity is full");
        }
        
        // Create student course enrollment
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudentUserId(selectionDTO.getStudentUserId());
        studentCourse.setCourseId(selectionDTO.getCourseId());
        studentCourse.setSemester(selectionDTO.getSemester());
        studentCourse.setCourseStatus(course.getCourseStatus());
        studentCourse.setIsActive(true);
        
        StudentCourse savedStudentCourse = studentCourseRepository.save(studentCourse);
        
        // Increment course enrolled count
        courseService.incrementEnrolledCount(selectionDTO.getCourseId());
        
        log.info("Student {} selected course {} ({})", 
                selectionDTO.getStudentUserId(), course.getCourseCode(), course.getCourseName());
        
        return mapToDTO(savedStudentCourse, course);
    }

    /**
     * Faculty/Admin replaces a student's selected course for a semester
     */
    @Transactional
    public StudentCourseSelectionDTO replaceStudentCourse(String studentUserId, Long oldCourseId, Long newCourseId, Integer semester) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        StudentCourse existingSelection = studentCourseRepository.findByStudentUserIdAndCourseId(studentUserId, oldCourseId)
                .orElseThrow(() -> new RuntimeException("Existing course selection not found"));

        Course oldCourse = courseRepository.findById(oldCourseId)
                .orElseThrow(() -> new RuntimeException("Old course not found"));

        Course newCourse = courseRepository.findById(newCourseId)
                .orElseThrow(() -> new RuntimeException("New course not found"));

        if (newCourseId.equals(oldCourseId)) {
            return mapToDTO(existingSelection, oldCourse);
        }

        if (studentCourseRepository.findByStudentUserIdAndCourseId(studentUserId, newCourseId).isPresent()) {
            throw new RuntimeException("New course is already selected by this student");
        }

        if (!existingSelection.getSemester().equals(semester)) {
            throw new RuntimeException("Semester does not match existing course selection");
        }

        if (!newCourse.getSemester().equals(semester)) {
            throw new RuntimeException("New course semester does not match selected semester");
        }

        validateDepartmentAndSemesterRules(student, newCourse, semester);
        validateSelectionLimits(studentUserId, semester, newCourse.getCourseStatus(), existingSelection);

        if (!courseService.isCourseAvailable(newCourseId)) {
            throw new RuntimeException("New course capacity is full");
        }

        existingSelection.setCourseId(newCourseId);
        existingSelection.setCourseStatus(newCourse.getCourseStatus());
        StudentCourse updatedSelection = studentCourseRepository.save(existingSelection);

        courseService.decrementEnrolledCount(oldCourseId);
        courseService.incrementEnrolledCount(newCourseId);

        log.info("Updated selected course for student {} from {} to {}", studentUserId, oldCourseId, newCourseId);
        return mapToDTO(updatedSelection, newCourse);
    }
    
    /**
     * Get all courses selected by a student for a semester
     */
    public List<StudentCourseSelectionDTO> getStudentCoursesBySemester(String studentUserId, Integer semester) {
        // Verify student exists
        studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<StudentCourse> selections = studentCourseRepository.findByStudentUserIdAndSemester(studentUserId, semester);
        Map<Long, Course> coursesById = loadCoursesByIds(selections);

        return selections
                .stream()
                .map(sc -> {
                Course course = coursesById.get(sc.getCourseId());
                if (course == null) {
                throw new RuntimeException("Course not found");
                }
                    return mapToDTO(sc, course);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get all core courses selected by a student
     */
    public List<StudentCourseSelectionDTO> getStudentCoreCourses(String studentUserId, Integer semester) {
        List<StudentCourse> selections = studentCourseRepository.findByStudentUserIdAndSemesterAndCourseStatus(
            studentUserId, semester, CourseStatus.CORE);
        Map<Long, Course> coursesById = loadCoursesByIds(selections);

        return selections
                .stream()
                .map(sc -> {
                Course course = coursesById.get(sc.getCourseId());
                if (course == null) {
                throw new RuntimeException("Course not found");
                }
                    return mapToDTO(sc, course);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get all elective courses selected by a student
     */
    public List<StudentCourseSelectionDTO> getStudentElectiveCourses(String studentUserId, Integer semester) {
        List<StudentCourse> selections = studentCourseRepository.findByStudentUserIdAndSemester(studentUserId, semester)
            .stream()
            .filter(sc -> sc.getCourseStatus() == CourseStatus.ELECTIVE || sc.getCourseStatus() == CourseStatus.CORE_ELECTIVE)
            .collect(Collectors.toList());
        Map<Long, Course> coursesById = loadCoursesByIds(selections);

        return selections
                .stream()
                .map(sc -> {
                Course course = coursesById.get(sc.getCourseId());
                if (course == null) {
                throw new RuntimeException("Course not found");
                }
                    return mapToDTO(sc, course);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Drop a course (student removes enrollment)
     */
    @Transactional
    public void dropCourse(String studentUserId, Long courseId) {
        StudentCourse studentCourse = studentCourseRepository.findByStudentUserIdAndCourseId(studentUserId, courseId)
                .orElseThrow(() -> new RuntimeException("Course enrollment not found"));
        
        studentCourseRepository.delete(studentCourse);
        
        // Decrement course enrolled count
        courseService.decrementEnrolledCount(courseId);
        
        log.info("Student {} dropped course ID {}", studentUserId, courseId);
    }
    
    /**
     * Get enrollment count for a course
     */
    public Long getCourseEnrollmentCount(Long courseId) {
        return studentCourseRepository.countByCourseId(courseId);
    }
    
    /**
     * Get course selection status for a student
     */
    public StudentCourseSelectionStatus getCourseSelectionStatus(String studentUserId, Integer semester) {
        long coreCount = studentCourseRepository.countByStudentUserIdAndSemesterAndCourseStatus(
                studentUserId, semester, CourseStatus.CORE);
        long coreElectiveCount = studentCourseRepository.countByStudentUserIdAndSemesterAndCourseStatus(
                studentUserId, semester, CourseStatus.CORE_ELECTIVE)
                + studentCourseRepository.countByStudentUserIdAndSemesterAndCourseStatus(
                studentUserId, semester, CourseStatus.ELECTIVE);
        long openElectiveCount = studentCourseRepository.countByStudentUserIdAndSemesterAndCourseStatus(
                studentUserId, semester, CourseStatus.OPEN_ELECTIVE);
        
        return new StudentCourseSelectionStatus(
                studentUserId,
                semester,
                (int) coreCount,
                (int) coreElectiveCount,
                (int) openElectiveCount,
                MAX_CORE_COURSES,
                MAX_CORE_ELECTIVE_COURSES,
                MAX_OPEN_ELECTIVE_COURSES
        );
    }

    private void validateDepartmentAndSemesterRules(Student student, Course course, Integer semester) {
        if (!course.getSemester().equals(semester)) {
            throw new RuntimeException("Course semester does not match selected semester");
        }

        String studentDepartment = student.getDepartment() == null ? "" : student.getDepartment().trim();
        String courseDepartment = course.getDepartment() == null ? "" : course.getDepartment().trim();

        if (course.getCourseStatus() == CourseStatus.OPEN_ELECTIVE) {
            if (studentDepartment.equalsIgnoreCase(courseDepartment)) {
                throw new RuntimeException("Open elective must be from another department");
            }
        } else {
            if (!studentDepartment.equalsIgnoreCase(courseDepartment)) {
                throw new RuntimeException("Core/Core Elective courses must belong to student's department");
            }
        }
    }

    private void validateSelectionLimits(String studentUserId, Integer semester, CourseStatus newStatus, StudentCourse replacingSelection) {
        List<StudentCourse> currentSelections = studentCourseRepository.findByStudentUserIdAndSemester(studentUserId, semester);

        int coreCount = 0;
        int coreElectiveCount = 0;
        int openElectiveCount = 0;

        for (StudentCourse selection : currentSelections) {
            if (replacingSelection != null && selection.getId().equals(replacingSelection.getId())) {
                continue;
            }

            if (selection.getCourseStatus() == CourseStatus.CORE) {
                coreCount++;
            } else if (selection.getCourseStatus() == CourseStatus.CORE_ELECTIVE || selection.getCourseStatus() == CourseStatus.ELECTIVE) {
                coreElectiveCount++;
            } else if (selection.getCourseStatus() == CourseStatus.OPEN_ELECTIVE) {
                openElectiveCount++;
            }
        }

        if (newStatus == CourseStatus.CORE) {
            coreCount++;
        } else if (newStatus == CourseStatus.CORE_ELECTIVE || newStatus == CourseStatus.ELECTIVE) {
            coreElectiveCount++;
        } else if (newStatus == CourseStatus.OPEN_ELECTIVE) {
            openElectiveCount++;
        }

        if (coreCount > MAX_CORE_COURSES) {
            throw new RuntimeException("Maximum " + MAX_CORE_COURSES + " core courses allowed per semester");
        }

        if (coreElectiveCount > MAX_CORE_ELECTIVE_COURSES) {
            throw new RuntimeException("Maximum " + MAX_CORE_ELECTIVE_COURSES + " core elective courses allowed per semester");
        }

        if (openElectiveCount > MAX_OPEN_ELECTIVE_COURSES) {
            throw new RuntimeException("Maximum " + MAX_OPEN_ELECTIVE_COURSES + " open elective course allowed per semester");
        }
    }
    
    private StudentCourseSelectionDTO mapToDTO(StudentCourse studentCourse, Course course) {
        StudentCourseSelectionDTO dto = new StudentCourseSelectionDTO();
        dto.setStudentUserId(studentCourse.getStudentUserId());
        dto.setCourseId(studentCourse.getCourseId());
        dto.setSemester(studentCourse.getSemester());
        dto.setCourseName(course.getCourseName());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseStatus(course.getCourseStatus());
        dto.setDepartment(course.getDepartment());
        return dto;
    }

    private Map<Long, Course> loadCoursesByIds(List<StudentCourse> selections) {
        Set<Long> courseIds = selections.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return courseRepository.findAllById(courseIds)
                .stream()
                .collect(Collectors.toMap(Course::getCourseId, course -> course));
    }
    
    /**
     * Inner class to represent course selection status
     */
    public static class StudentCourseSelectionStatus {
        public String studentUserId;
        public Integer semester;
        public Integer coreCoursesSelected;
        public Integer coreElectiveCoursesSelected;
        public Integer openElectiveCoursesSelected;
        public Integer maxCoreAllowed;
        public Integer maxCoreElectiveAllowed;
        public Integer maxOpenElectiveAllowed;
        
        public StudentCourseSelectionStatus(String studentUserId, Integer semester, 
                                          Integer coreCoursesSelected, Integer coreElectiveCoursesSelected,
                                          Integer openElectiveCoursesSelected,
                                          Integer maxCoreAllowed, Integer maxCoreElectiveAllowed,
                                          Integer maxOpenElectiveAllowed) {
            this.studentUserId = studentUserId;
            this.semester = semester;
            this.coreCoursesSelected = coreCoursesSelected;
            this.coreElectiveCoursesSelected = coreElectiveCoursesSelected;
            this.openElectiveCoursesSelected = openElectiveCoursesSelected;
            this.maxCoreAllowed = maxCoreAllowed;
            this.maxCoreElectiveAllowed = maxCoreElectiveAllowed;
            this.maxOpenElectiveAllowed = maxOpenElectiveAllowed;
        }
    }
}
