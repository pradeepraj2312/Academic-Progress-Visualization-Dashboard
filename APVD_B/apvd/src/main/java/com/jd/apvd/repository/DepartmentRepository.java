package com.jd.apvd.repository;

import com.jd.apvd.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentName(String departmentName);
    Optional<Department> findByDepartmentCode(String departmentCode);
    boolean existsByDepartmentName(String departmentName);
    boolean existsByDepartmentCode(String departmentCode);
}
