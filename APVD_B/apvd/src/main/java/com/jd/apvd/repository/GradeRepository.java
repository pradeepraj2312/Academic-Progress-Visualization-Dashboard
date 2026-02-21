package com.jd.apvd.repository;

import com.jd.apvd.entity.Grade;
import com.jd.apvd.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentUserId(String studentUserId);
    List<Grade> findByStudentUserIdAndSemester(String studentUserId, Integer semester);
    List<Grade> findByStudentUserIdAndDepartment(String studentUserId, String department);
    Optional<Grade> findByStudentUserIdAndCourseIdAndSemester(String studentUserId, Long courseId, Integer semester);
    List<Grade> findByCourseId(Long courseId);
    List<Grade> findByDepartmentAndSemester(String department, Integer semester);
    List<Grade> findByStudentUserIdAndCourseStatus(String studentUserId, CourseStatus courseStatus);
    
    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.studentUserId = :studentUserId AND g.semester = :semester")
    Double calculateSGPAForSemester(@Param("studentUserId") String studentUserId, @Param("semester") Integer semester);
    
    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.studentUserId = :studentUserId")
    Double calculateCGPA(@Param("studentUserId") String studentUserId);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.studentUserId = :studentUserId AND g.letterGrade = 'F'")
    Long countFailedCourses(@Param("studentUserId") String studentUserId);
}
