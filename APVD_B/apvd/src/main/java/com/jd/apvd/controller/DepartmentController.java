package com.jd.apvd.controller;

import com.jd.apvd.dto.DepartmentDTO;
import com.jd.apvd.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    
    private final DepartmentService departmentService;
    
    /**
     * Admin adds a new department
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> addDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        DepartmentDTO createdDepartment = departmentService.addDepartment(departmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }
    
    /**
     * Admin updates department details
     */
    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable Long departmentId,
            @Valid @RequestBody DepartmentDTO departmentDTO) {
        DepartmentDTO updatedDepartment = departmentService.updateDepartment(departmentId, departmentDTO);
        return ResponseEntity.ok(updatedDepartment);
    }
    
    /**
     * Get department by ID (any authenticated user)
     */
    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long departmentId) {
        DepartmentDTO department = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(department);
    }
    
    /**
     * Get all departments (any authenticated user)
     */
    @GetMapping("/all")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }
    
    /**
     * Admin deletes a department
     */
    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long departmentId) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.ok("Department deleted successfully");
    }
}
