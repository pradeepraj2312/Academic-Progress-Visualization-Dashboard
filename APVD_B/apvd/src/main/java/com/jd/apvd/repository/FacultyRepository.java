package com.jd.apvd.repository;

import com.jd.apvd.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByUserId(String userId);
    Optional<Faculty> findByUserEmail(String userEmail);
    List<Faculty> findByDepartment(String department);
    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
}
