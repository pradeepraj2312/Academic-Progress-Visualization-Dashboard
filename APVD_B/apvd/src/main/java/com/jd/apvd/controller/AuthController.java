package com.jd.apvd.controller;

import com.jd.apvd.dto.LoginResponseDTO;
import com.jd.apvd.dto.UserLoginDTO;
import com.jd.apvd.dto.UserRegisterDTO;
import com.jd.apvd.dto.UserResponseDTO;
import com.jd.apvd.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        log.info("Registering new user with email: {}", registerDTO.getUserEmail());
        UserResponseDTO response = authService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Login with userID or userEmail
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        log.info("Login attempt for: {}", loginDTO.getUserIdOrEmail());
        LoginResponseDTO response = authService.login(loginDTO.getUserIdOrEmail(), loginDTO.getPassword());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint to verify JWT token
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyToken() {
        return ResponseEntity.ok("Token is valid");
    }
}
