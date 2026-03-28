package com.jd.apvd.repository;

import com.jd.apvd.entity.StudentMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentMarksRepository extends JpaRepository<StudentMarks, Long> {
    Optional<StudentMarks> findByUserIdAndSemester(String userId, Integer semester);
    List<StudentMarks> findByUserId(String userId);
    long deleteByUserId(String userId);
    List<StudentMarks> findByUserIdAndSemesterOrderByUpdatedAtDesc(String userId, Integer semester);
    Optional<StudentMarks> findByUserEmail(String userEmail);
    List<StudentMarks> findBySemester(Integer semester);
    
    @Query("SELECT AVG(sm.sgpa) FROM StudentMarks sm WHERE sm.userId = :userId AND sm.sgpa > 0")
    Double calculateCGPA(@Param("userId") String userId);
}
