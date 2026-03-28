package com.jd.apvd.service;

import com.jd.apvd.dto.CourseDTO;
import com.jd.apvd.dto.BulkUploadResultDTO;
import com.jd.apvd.entity.Course;
import com.jd.apvd.entity.CourseStatus;
import com.jd.apvd.entity.UserRole;
import com.jd.apvd.entity.Users;
import com.jd.apvd.util.ExcelImportUtils;
import com.jd.apvd.repository.CourseRepository;
import com.jd.apvd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    /**
     * Faculty or Admin creates a new course
     */
    @Transactional
    public CourseDTO addCourse(CourseDTO courseDTO) {
        // Validate course code uniqueness
        if (courseRepository.existsByCourseCode(courseDTO.getCourseCode())) {
            throw new RuntimeException("Course code already exists");
        }
        
        // Create course entity
        Course course = new Course();
        course.setCourseCode(courseDTO.getCourseCode());
        course.setCourseName(courseDTO.getCourseName());
        course.setDepartment(courseDTO.getDepartment());
        course.setSemester(courseDTO.getSemester());
        course.setCourseStatus(courseDTO.getCourseStatus());
        course.setFacultyUserId(courseDTO.getFacultyUserId());
        course.setDescription(courseDTO.getDescription());
        course.setCredits(courseDTO.getCredits());
        course.setCapacity(courseDTO.getCapacity());
        course.setEnrolledCount(0);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created: {} ({})", savedCourse.getCourseCode(), savedCourse.getCourseName());
        
        return mapCourseToDTO(savedCourse);
    }

    /**
     * Bulk upload courses from Excel file.
     * Expected columns: courseCode, courseName, department, semester, courseStatus, facultyUserId, description, credits, capacity
     */
    @Transactional
    public BulkUploadResultDTO bulkUploadCourses(MultipartFile file) {
        validateExcelFile(file);
        BulkUploadResultDTO result = new BulkUploadResultDTO();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (ExcelImportUtils.isRowEmpty(row)) {
                    continue;
                }

                result.incrementTotalRows();

                try {
                    CourseDTO dto = new CourseDTO();
                    dto.setCourseCode(ExcelImportUtils.getCellString(row, 0));
                    dto.setCourseName(ExcelImportUtils.getCellString(row, 1));
                    dto.setDepartment(ExcelImportUtils.getCellString(row, 2));
                    dto.setSemester(ExcelImportUtils.getCellInteger(row, 3));

                    String statusValue = ExcelImportUtils.getCellString(row, 4);
                    dto.setCourseStatus(CourseStatus.valueOf(statusValue.toUpperCase()));

                    dto.setFacultyUserId(ExcelImportUtils.getCellString(row, 5));
                    dto.setDescription(ExcelImportUtils.getCellString(row, 6));
                    dto.setCredits(ExcelImportUtils.getCellInteger(row, 7));
                    dto.setCapacity(ExcelImportUtils.getCellInteger(row, 8));

                    addCourse(dto);
                    result.incrementSuccessCount();
                } catch (Exception ex) {
                    result.addError("Row " + (rowIndex + 1) + ": " + ex.getMessage());
                }
            }

            return result;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read uploaded Excel file", ex);
        }
    }
    
    /**
     * Update course details (Faculty/Admin)
     */
    @Transactional
    public CourseDTO updateCourse(Long courseId, CourseDTO courseDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setCourseName(courseDTO.getCourseName());
        course.setDepartment(courseDTO.getDepartment());
        course.setSemester(courseDTO.getSemester());
        course.setCourseStatus(courseDTO.getCourseStatus());
        course.setDescription(courseDTO.getDescription());
        course.setCredits(courseDTO.getCredits());
        course.setCapacity(courseDTO.getCapacity());
        
        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated: {}", updatedCourse.getCourseCode());
        
        return mapCourseToDTO(updatedCourse);
    }
    
    /**
     * Get course by ID
     */
    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mapCourseToDTO(course);
    }
    
    /**
     * Get course by course code
     */
    public CourseDTO getCourseByCourseCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return mapCourseToDTO(course);
    }

    /**
     * Get all courses
     */
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all courses by department and semester
     */
    public List<CourseDTO> getCoursesByDepartmentAndSemester(String department, Integer semester) {
        return courseRepository.findByDepartmentAndSemester(department, semester)
                .stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get core courses for a department and semester
     */
    public List<CourseDTO> getCoreCoursesByDepartmentAndSemester(String department, Integer semester) {
        return courseRepository.findByDepartmentAndSemesterAndCourseStatus(department, semester, CourseStatus.CORE)
                .stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get elective courses for a department and semester
     */
    public List<CourseDTO> getElectiveCoursesByDepartmentAndSemester(String department, Integer semester) {
        return courseRepository.findByDepartmentAndSemesterAndCourseStatus(department, semester, CourseStatus.ELECTIVE)
                .stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all courses by faculty
     */
    public List<CourseDTO> getCoursesByFaculty(String facultyUserId) {
        return courseRepository.findByFacultyUserId(facultyUserId)
                .stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Reassign all courses from one faculty to another (Admin only)
     */
    @Transactional
    public int reassignCoursesToFaculty(String fromFacultyUserId, String toFacultyUserId) {
        if (fromFacultyUserId == null || fromFacultyUserId.trim().isEmpty()) {
            throw new RuntimeException("Source faculty user ID is required");
        }

        if (toFacultyUserId == null || toFacultyUserId.trim().isEmpty()) {
            throw new RuntimeException("Target faculty user ID is required");
        }

        if (fromFacultyUserId.equals(toFacultyUserId)) {
            throw new RuntimeException("Source and target faculty cannot be the same");
        }

        Users fromFaculty = userRepository.findByUserId(fromFacultyUserId)
                .orElseThrow(() -> new RuntimeException("Source faculty not found"));
        if (fromFaculty.getRole() != UserRole.FACULTY) {
            throw new RuntimeException("Source user is not a faculty");
        }

        Users toFaculty = userRepository.findByUserId(toFacultyUserId)
                .orElseThrow(() -> new RuntimeException("Target faculty not found"));
        if (toFaculty.getRole() != UserRole.FACULTY) {
            throw new RuntimeException("Target user is not a faculty");
        }

        List<Course> courses = courseRepository.findByFacultyUserId(fromFacultyUserId);
        for (Course course : courses) {
            course.setFacultyUserId(toFacultyUserId);
        }

        courseRepository.saveAll(courses);
        log.info("Reassigned {} courses from faculty {} to {}", courses.size(), fromFacultyUserId, toFacultyUserId);
        return courses.size();
    }
    
    /**
     * Get all courses by department
     */
    public List<CourseDTO> getCoursesByDepartment(String department) {
        return courseRepository.findByDepartment(department)
                .stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all courses by semester
     */
    public List<CourseDTO> getCoursesBySemester(Integer semester) {
        return courseRepository.findBySemester(semester)
                .stream()
                .map(this::mapCourseToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete course (Admin only)
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        courseRepository.delete(course);
        log.info("Course deleted: {}", course.getCourseCode());
    }
    
    /**
     * Check if course is available (capacity not reached)
     */
    public boolean isCourseAvailable(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getCapacity() == null) {
            return true;  // No capacity limit
        }
        
        return course.getEnrolledCount() < course.getCapacity();
    }
    
    /**
     * Increment enrolled count
     */
    @Transactional
    public void incrementEnrolledCount(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getCapacity() != null && course.getEnrolledCount() >= course.getCapacity()) {
            throw new RuntimeException("Course capacity reached");
        }
        
        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);
    }
    
    /**
     * Decrement enrolled count
     */
    @Transactional
    public void decrementEnrolledCount(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (course.getEnrolledCount() > 0) {
            course.setEnrolledCount(course.getEnrolledCount() - 1);
            courseRepository.save(course);
        }
    }
    
    private CourseDTO mapCourseToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDepartment(course.getDepartment());
        dto.setSemester(course.getSemester());
        dto.setCourseStatus(course.getCourseStatus());
        dto.setFacultyUserId(course.getFacultyUserId());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setCapacity(course.getCapacity());
        dto.setEnrolledCount(course.getEnrolledCount());
        return dto;
    }

    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please upload a non-empty Excel file");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            throw new RuntimeException("Only .xlsx or .xls files are supported");
        }
    }
}

