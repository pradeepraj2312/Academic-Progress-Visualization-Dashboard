package com.jd.apvd.repository;

import com.jd.apvd.entity.StudentCourse;
import com.jd.apvd.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByStudentUserId(String studentUserId);
    List<StudentCourse> findByStudentUserIdAndSemester(String studentUserId, Integer semester);
    List<StudentCourse> findByStudentUserIdAndCourseStatus(String studentUserId, CourseStatus courseStatus);
    List<StudentCourse> findByStudentUserIdAndSemesterAndCourseStatus(String studentUserId, Integer semester, CourseStatus courseStatus);
    Optional<StudentCourse> findByStudentUserIdAndCourseId(String studentUserId, Long courseId);
    Long countByStudentUserIdAndSemesterAndCourseStatus(String studentUserId, Integer semester, CourseStatus courseStatus);
    Long countByCourseId(Long courseId);
}
