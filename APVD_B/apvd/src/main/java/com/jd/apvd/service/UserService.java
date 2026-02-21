package com.jd.apvd.service;

import com.jd.apvd.dto.UserRegisterDTO;
import com.jd.apvd.dto.UserResponseDTO;
import com.jd.apvd.entity.Admin;
import com.jd.apvd.entity.Faculty;
import com.jd.apvd.entity.Student;
import com.jd.apvd.entity.Users;
import com.jd.apvd.entity.UserRole;
import com.jd.apvd.repository.AdminRepository;
import com.jd.apvd.repository.FacultyRepository;
import com.jd.apvd.repository.StudentRepository;
import com.jd.apvd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Admin adds a student
     * User ID must be provided in the request
     */
    @Transactional
    public UserResponseDTO adminAddStudent(UserRegisterDTO registerDTO) {
        // Validate userId is provided
        if (registerDTO.getUserId() == null || registerDTO.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        
        // Check if userId already exists
        if (userRepository.existsByUserId(registerDTO.getUserId())) {
            throw new RuntimeException("User ID already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByUserEmail(registerDTO.getUserEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create Users entity with provided userId
        Users user = new Users();
        user.setUserId(registerDTO.getUserId());  // Use provided user ID
        user.setUsername(registerDTO.getUsername());
        user.setUserEmail(registerDTO.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(registerDTO.getUserPassword()));
        user.setMobile(registerDTO.getMobile());
        user.setRole(UserRole.STUDENT);
        
        Users savedUser = userRepository.save(user);
        log.info("Student user created with ID: {}", savedUser.getUserId());
        
        // Create Student entity
        Student student = new Student();
        student.setUserId(savedUser.getUserId());
        student.setUsername(savedUser.getUsername());
        student.setUserEmail(savedUser.getUserEmail());
        student.setMobile(savedUser.getMobile());
        student.setEnrollmentNumber(registerDTO.getEnrollmentNumber());
        student.setDepartment(registerDTO.getDepartment() != null && !registerDTO.getDepartment().trim().isEmpty() 
            ? registerDTO.getDepartment() : "management");
        student.setYearOfStudying(registerDTO.getYearOfStudying());
        studentRepository.save(student);
        
        return mapUserToResponseDTO(savedUser);
    }
    
    /**
     * Admin adds a faculty
     * User ID must be provided in the request
     */
    @Transactional
    public UserResponseDTO adminAddFaculty(UserRegisterDTO registerDTO) {
        // Validate userId is provided
        if (registerDTO.getUserId() == null || registerDTO.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        
        // Check if userId already exists
        if (userRepository.existsByUserId(registerDTO.getUserId())) {
            throw new RuntimeException("User ID already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByUserEmail(registerDTO.getUserEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create Users entity with provided userId
        Users user = new Users();
        user.setUserId(registerDTO.getUserId());  // Use provided user ID
        user.setUsername(registerDTO.getUsername());
        user.setUserEmail(registerDTO.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(registerDTO.getUserPassword()));
        user.setMobile(registerDTO.getMobile());
        user.setRole(UserRole.FACULTY);
        
        Users savedUser = userRepository.save(user);
        log.info("Faculty user created with ID: {}", savedUser.getUserId());
        
        // Create Faculty entity
        Faculty faculty = new Faculty();
        faculty.setUserId(savedUser.getUserId());
        faculty.setUsername(savedUser.getUsername());
        faculty.setUserEmail(savedUser.getUserEmail());
        faculty.setMobile(savedUser.getMobile());
        faculty.setDepartment(registerDTO.getDepartment() != null && !registerDTO.getDepartment().trim().isEmpty() 
            ? registerDTO.getDepartment() : "management");
        faculty.setQualification(registerDTO.getQualification());
        faculty.setSpecialization(registerDTO.getSpecialization());
        facultyRepository.save(faculty);
        
        return mapUserToResponseDTO(savedUser);
    }
    
    /**
     * Faculty adds a student
     * User ID must be provided in the request
     */
    @Transactional
    public UserResponseDTO facultyAddStudent(UserRegisterDTO registerDTO) {
        // Validate userId is provided
        if (registerDTO.getUserId() == null || registerDTO.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        
        // Check if userId already exists
        if (userRepository.existsByUserId(registerDTO.getUserId())) {
            throw new RuntimeException("User ID already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByUserEmail(registerDTO.getUserEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create Users entity with provided userId
        Users user = new Users();
        user.setUserId(registerDTO.getUserId());  // Use provided user ID
        user.setUsername(registerDTO.getUsername());
        user.setUserEmail(registerDTO.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(registerDTO.getUserPassword()));
        user.setMobile(registerDTO.getMobile());
        user.setRole(UserRole.STUDENT);
        
        Users savedUser = userRepository.save(user);
        log.info("Student user created by faculty with ID: {}", savedUser.getUserId());
        
        // Create Student entity
        Student student = new Student();
        student.setUserId(savedUser.getUserId());
        student.setUsername(savedUser.getUsername());
        student.setUserEmail(savedUser.getUserEmail());
        student.setMobile(savedUser.getMobile());
        student.setEnrollmentNumber(registerDTO.getEnrollmentNumber());
        student.setDepartment(registerDTO.getDepartment() != null && !registerDTO.getDepartment().trim().isEmpty() 
            ? registerDTO.getDepartment() : "management");
        student.setYearOfStudying(registerDTO.getYearOfStudying());
        studentRepository.save(student);
        
        return mapUserToResponseDTO(savedUser);
    }
    
    /**
     * Get all users by role
     */
        public List<UserResponseDTO> getUsersByRole(UserRole role, String requesterUserId) {
        List<Users> users = userRepository.findByRole(role);

        if (role == UserRole.STUDENT) {
            List<String> userIds = users.stream().map(Users::getUserId).collect(Collectors.toList());
            Map<String, Student> studentsByUserId = studentRepository.findByUserIdIn(userIds).stream()
                    .collect(Collectors.toMap(Student::getUserId, Function.identity()));

            Users requester = requesterUserId != null
                ? userRepository.findByUserId(requesterUserId).orElse(null)
                : null;

            Set<String> allowedStudentUserIds = null;
            if (requester != null && requester.getRole() == UserRole.FACULTY) {
            String facultyDepartment = facultyRepository.findByUserId(requesterUserId)
                .map(Faculty::getDepartment)
                .orElse(null);

            if (facultyDepartment == null || facultyDepartment.trim().isEmpty()) {
                return List.of();
            }

            allowedStudentUserIds = studentsByUserId.values().stream()
                .filter(student -> facultyDepartment.equalsIgnoreCase(student.getDepartment()))
                .map(Student::getUserId)
                .collect(Collectors.toSet());
            }

            final Set<String> finalAllowedStudentUserIds = allowedStudentUserIds;

            return users.stream()
                .filter(user -> finalAllowedStudentUserIds == null || finalAllowedStudentUserIds.contains(user.getUserId()))
                    .map(user -> {
                        UserResponseDTO dto = new UserResponseDTO();
                        dto.setUserId(user.getUserId());
                        dto.setUsername(user.getUsername());
                        dto.setUserEmail(user.getUserEmail());
                        dto.setMobile(user.getMobile());
                        Student student = studentsByUserId.get(user.getUserId());
                        if (student != null) {
                            dto.setEnrollmentNumber(student.getEnrollmentNumber());
                            dto.setDepartment(student.getDepartment());
                            dto.setYearOfStudying(student.getYearOfStudying());
                        }
                        dto.setRole(user.getRole());
                        dto.setCreatedAt(user.getCreatedAt());
                        dto.setUpdatedAt(user.getUpdatedAt());
                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        return users.stream()
                .map(this::mapUserToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user by user ID
     */
    public UserResponseDTO getUserByUserId(String userId) {
        Users user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapUserToResponseDTO(user);
    }
    
    /**
     * Get user by email
     */
    public UserResponseDTO getUserByEmail(String email) {
        Users user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapUserToResponseDTO(user);
    }
    
    /**
     * Update user
     */
    @Transactional
    public UserResponseDTO updateUser(String userId, UserRegisterDTO updateDTO) {
        Users user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setUsername(updateDTO.getUsername());
        user.setMobile(updateDTO.getMobile());
        
        Users updatedUser = userRepository.save(user);
        
        // Update role-specific information
        switch (user.getRole()) {
            case STUDENT:
                Student student = studentRepository.findByUserId(userId)
                        .orElse(new Student());
                student.setUsername(updateDTO.getUsername());
                student.setMobile(updateDTO.getMobile());
                student.setDepartment(updateDTO.getDepartment());
                student.setYearOfStudying(updateDTO.getYearOfStudying());
                studentRepository.save(student);
                break;
            case FACULTY:
                Faculty faculty = facultyRepository.findByUserId(userId)
                        .orElse(new Faculty());
                faculty.setUsername(updateDTO.getUsername());
                faculty.setMobile(updateDTO.getMobile());
                faculty.setDepartment(updateDTO.getDepartment());
                faculty.setQualification(updateDTO.getQualification());
                faculty.setSpecialization(updateDTO.getSpecialization());
                facultyRepository.save(faculty);
                break;
            case ADMIN:
                Admin admin = adminRepository.findByUserId(userId)
                        .orElse(new Admin());
                admin.setUsername(updateDTO.getUsername());
                admin.setMobile(updateDTO.getMobile());
                admin.setDepartment(updateDTO.getDepartment());
                adminRepository.save(admin);
                break;
        }
        
        return mapUserToResponseDTO(updatedUser);
    }
    
    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(String userId) {
        Users user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete role-specific record
        switch (user.getRole()) {
            case STUDENT:
                studentRepository.findByUserId(userId).ifPresent(studentRepository::delete);
                break;
            case FACULTY:
                facultyRepository.findByUserId(userId).ifPresent(facultyRepository::delete);
                break;
            case ADMIN:
                adminRepository.findByUserId(userId).ifPresent(adminRepository::delete);
                break;
        }
        
        // Delete user
        userRepository.delete(user);
    }
    
    private UserResponseDTO mapUserToResponseDTO(Users user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setUserEmail(user.getUserEmail());
        dto.setMobile(user.getMobile());
        switch (user.getRole()) {
            case STUDENT:
                studentRepository.findByUserId(user.getUserId()).ifPresent(student -> {
                    dto.setEnrollmentNumber(student.getEnrollmentNumber());
                    dto.setDepartment(student.getDepartment());
                    dto.setYearOfStudying(student.getYearOfStudying());
                });
                break;
            case FACULTY:
                facultyRepository.findByUserId(user.getUserId()).ifPresent(faculty ->
                        dto.setDepartment(faculty.getDepartment()));
                break;
            case ADMIN:
                adminRepository.findByUserId(user.getUserId()).ifPresent(admin ->
                        dto.setDepartment(admin.getDepartment()));
                break;
        }
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
