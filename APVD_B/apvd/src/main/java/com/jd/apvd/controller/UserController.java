package com.jd.apvd.controller;

import com.jd.apvd.dto.UserRegisterDTO;
import com.jd.apvd.dto.UserResponseDTO;
import com.jd.apvd.dto.BulkUploadResultDTO;
import com.jd.apvd.entity.UserRole;
import com.jd.apvd.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:*", "https://*.development.catalystappsail.in"})
public class UserController {
    
    private final UserService userService;
    
    /**
     * Admin adds a student
     */
    @PostMapping("/admin/add-student")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminAddStudent(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("Admin adding new student with email: {}", registerDTO.getUserEmail());
        UserResponseDTO response = userService.adminAddStudent(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin bulk uploads students from Excel
     */
    @PostMapping("/admin/upload-students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResultDTO> adminUploadStudents(@RequestParam("file") MultipartFile file) {
        log.info("Admin uploading students via Excel: {}", file.getOriginalFilename());
        BulkUploadResultDTO response = userService.bulkUploadStudents(file);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Admin adds a faculty
     */
    @PostMapping("/admin/add-faculty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminAddFaculty(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("Admin adding new faculty with email: {}", registerDTO.getUserEmail());
        UserResponseDTO response = userService.adminAddFaculty(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin bulk uploads faculties from Excel
     */
    @PostMapping("/admin/upload-faculties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResultDTO> adminUploadFaculties(@RequestParam("file") MultipartFile file) {
        log.info("Admin uploading faculties via Excel: {}", file.getOriginalFilename());
        BulkUploadResultDTO response = userService.bulkUploadFaculties(file);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Faculty adds a student
     */
    @PostMapping("/faculty/add-student")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<UserResponseDTO> facultyAddStudent(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("Faculty adding new student with email: {}", registerDTO.getUserEmail());
        UserResponseDTO response = userService.facultyAddStudent(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all users by role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable String role, Authentication authentication) {
        log.info("Fetching users with role: {}", role);
        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        String requesterUserId = authentication != null ? String.valueOf(authentication.getPrincipal()) : null;
        List<UserResponseDTO> users = userService.getUsersByRole(userRole, requesterUserId);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by userId
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserByUserId(@PathVariable String userId) {
        log.info("Fetching user with userId: {}", userId);
        UserResponseDTO user = userService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Update user
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserRegisterDTO updateDTO) {
        log.info("Updating user with userId: {}", userId);
        UserResponseDTO user = userService.updateUser(userId, updateDTO);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        log.info("Deleting user with userId: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}
