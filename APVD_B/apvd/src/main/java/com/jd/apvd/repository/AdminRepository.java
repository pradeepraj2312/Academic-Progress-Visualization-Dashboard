package com.jd.apvd.repository;

import com.jd.apvd.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserId(String userId);
    Optional<Admin> findByUserEmail(String userEmail);
    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
}
