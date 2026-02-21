package com.jd.apvd.repository;

import com.jd.apvd.entity.Users;
import com.jd.apvd.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    Optional<Users> findByUserId(String userId);
    Optional<Users> findByUserEmail(String userEmail);
    Optional<Users> findByUserIdOrUserEmail(String userId, String userEmail);
    List<Users> findByRole(UserRole role);
    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
}
