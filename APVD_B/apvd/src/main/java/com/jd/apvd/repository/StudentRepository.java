package com.jd.apvd.repository;

import com.jd.apvd.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(String userId);
    List<Student> findByUserIdIn(List<String> userIds);
    Optional<Student> findByUserEmail(String userEmail);
    List<Student> findByDepartment(String department);
    List<Student> findByYearOfStudying(Integer yearOfStudying);
    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
}
