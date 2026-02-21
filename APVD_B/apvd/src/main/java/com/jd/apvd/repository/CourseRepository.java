package com.jd.apvd.repository;

import com.jd.apvd.entity.Course;
import com.jd.apvd.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByDepartment(String department);
    List<Course> findBySemester(Integer semester);
    List<Course> findByDepartmentAndSemester(String department, Integer semester);
    List<Course> findByDepartmentAndSemesterAndCourseStatus(String department, Integer semester, CourseStatus courseStatus);
    List<Course> findByFacultyUserId(String facultyUserId);
    List<Course> findByCourseStatus(CourseStatus courseStatus);
    List<Course> findByDepartmentAndCourseStatus(String department, CourseStatus courseStatus);
    boolean existsByCourseCode(String courseCode);
}
