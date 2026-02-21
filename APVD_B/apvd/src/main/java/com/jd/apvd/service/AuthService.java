package com.jd.apvd.service;

import com.jd.apvd.dto.LoginResponseDTO;
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
import com.jd.apvd.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Register a new user
     * Based on the role, the user is added to the respective table (STUDENT, FACULTY, ADMIN)
     * User ID is provided by the user, not auto-generated
     */
    @Transactional
    public UserResponseDTO register(UserRegisterDTO registerDTO) {
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
        user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : UserRole.STUDENT);
        
        Users savedUser = userRepository.save(user);
        log.info("User registered with ID: {}", savedUser.getUserId());
        
        // Create role-specific entity
        switch (savedUser.getRole()) {
            case STUDENT:
                createStudent(savedUser, registerDTO);
                break;
            case FACULTY:
                createFaculty(savedUser, registerDTO);
                break;
            case ADMIN:
                createAdmin(savedUser, registerDTO);
                break;
        }
        
        return mapUserToResponseDTO(savedUser);
    }
    
    /**
     * Login with userID or userEmail and password
     */
    public LoginResponseDTO login(String userIdOrEmail, String password) {
        // Find user by userId or userEmail
        Optional<Users> userOpt = userRepository.findByUserIdOrUserEmail(userIdOrEmail, userIdOrEmail);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        
        Users user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getUserPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getUserEmail(), user.getRole().name());
        long expiresIn = 86400000L;  // 24 hours
        
        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setUserEmail(user.getUserEmail());
        response.setMobile(user.getMobile());
        response.setRole(user.getRole());
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        
        return response;
    }
    
    /**
     * Create a student record with department (default: management)
     */
    private void createStudent(Users user, UserRegisterDTO registerDTO) {
        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setUsername(user.getUsername());
        student.setUserEmail(user.getUserEmail());
        student.setMobile(user.getMobile());
        student.setEnrollmentNumber(registerDTO.getEnrollmentNumber());
        student.setDepartment(registerDTO.getDepartment() != null && !registerDTO.getDepartment().trim().isEmpty() 
            ? registerDTO.getDepartment() : "management");
        student.setYearOfStudying(registerDTO.getYearOfStudying());
        studentRepository.save(student);
        log.info("Student created for user ID: {}", user.getUserId());
    }
    
    /**
     * Create a faculty record with department (default: management)
     */
    private void createFaculty(Users user, UserRegisterDTO registerDTO) {
        Faculty faculty = new Faculty();
        faculty.setUserId(user.getUserId());
        faculty.setUsername(user.getUsername());
        faculty.setUserEmail(user.getUserEmail());
        faculty.setMobile(user.getMobile());
        faculty.setDepartment(registerDTO.getDepartment() != null && !registerDTO.getDepartment().trim().isEmpty() 
            ? registerDTO.getDepartment() : "management");
        faculty.setQualification(registerDTO.getQualification());
        faculty.setSpecialization(registerDTO.getSpecialization());
        facultyRepository.save(faculty);
        log.info("Faculty created for user ID: {}", user.getUserId());
    }
    
    /**
     * Create an admin record with department (default: management)
     */
    private void createAdmin(Users user, UserRegisterDTO registerDTO) {
        Admin admin = new Admin();
        admin.setUserId(user.getUserId());
        admin.setUsername(user.getUsername());
        admin.setUserEmail(user.getUserEmail());
        admin.setMobile(user.getMobile());
        admin.setDepartment(registerDTO.getDepartment() != null && !registerDTO.getDepartment().trim().isEmpty() 
            ? registerDTO.getDepartment() : "management");
        adminRepository.save(admin);
        log.info("Admin created for user ID: {}", user.getUserId());
    }
    
    private UserResponseDTO mapUserToResponseDTO(Users user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setUserEmail(user.getUserEmail());
        dto.setMobile(user.getMobile());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
