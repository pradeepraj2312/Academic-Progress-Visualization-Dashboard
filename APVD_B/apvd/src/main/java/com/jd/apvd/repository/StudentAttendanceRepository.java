package com.jd.apvd.repository;

import com.jd.apvd.entity.Session;
import com.jd.apvd.entity.StudentAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, Long> {
    List<StudentAttendance> findByUserId(String userId);
    long deleteByUserId(String userId);
    List<StudentAttendance> findByUserIdAndAttendanceDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<StudentAttendance> findByUserIdOrderByAttendanceDateDesc(String userId);
    List<StudentAttendance> findByAttendanceDate(LocalDate date);
    List<StudentAttendance> findByAttendanceDateAndSession(LocalDate date, Session session);
    Optional<StudentAttendance> findByUserIdAndAttendanceDateAndSession(String userId, LocalDate date, Session session);
    long countByUserIdAndAttendanceDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT sa FROM StudentAttendance sa WHERE sa.userId IN :userIds ORDER BY sa.attendanceDate DESC, sa.session DESC")
    List<StudentAttendance> findByUserIdIn(@Param("userIds") List<String> userIds);
}
