package com.jd.apvd.service;

import com.jd.apvd.dto.DepartmentDTO;
import com.jd.apvd.dto.BulkUploadResultDTO;
import com.jd.apvd.entity.Department;
import com.jd.apvd.util.ExcelImportUtils;
import com.jd.apvd.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    /**
     * Add a new department (Admin only)
     */
    @Transactional
    public DepartmentDTO addDepartment(DepartmentDTO departmentDTO) {
        // Check if department already exists
        if (departmentRepository.existsByDepartmentName(departmentDTO.getDepartmentName())) {
            throw new RuntimeException("Department with name '" + departmentDTO.getDepartmentName() + "' already exists");
        }
        
        if (departmentDTO.getDepartmentCode() != null && 
            departmentRepository.existsByDepartmentCode(departmentDTO.getDepartmentCode())) {
            throw new RuntimeException("Department with code '" + departmentDTO.getDepartmentCode() + "' already exists");
        }
        
        Department department = new Department();
        department.setDepartmentName(departmentDTO.getDepartmentName());
        department.setDepartmentCode(departmentDTO.getDepartmentCode());
        department.setDescription(departmentDTO.getDescription());
        
        Department savedDepartment = departmentRepository.save(department);
        log.info("Department added: {}", savedDepartment.getDepartmentName());
        
        return mapDepartmentToDTO(savedDepartment);
    }

    /**
     * Bulk upload departments from Excel file.
     * Expected columns: departmentName, departmentCode, description
     */
    @Transactional
    public BulkUploadResultDTO bulkUploadDepartments(MultipartFile file) {
        validateExcelFile(file);
        BulkUploadResultDTO result = new BulkUploadResultDTO();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (ExcelImportUtils.isRowEmpty(row)) {
                    continue;
                }

                result.incrementTotalRows();

                try {
                    DepartmentDTO dto = new DepartmentDTO();
                    dto.setDepartmentName(ExcelImportUtils.getCellString(row, 0));
                    dto.setDepartmentCode(ExcelImportUtils.getCellString(row, 1));
                    dto.setDescription(ExcelImportUtils.getCellString(row, 2));

                    addDepartment(dto);
                    result.incrementSuccessCount();
                } catch (Exception ex) {
                    result.addError("Row " + (rowIndex + 1) + ": " + ex.getMessage());
                }
            }

            return result;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read uploaded Excel file", ex);
        }
    }
    
    /**
     * Update department details
     */
    @Transactional
    public DepartmentDTO updateDepartment(Long departmentId, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
        
        // Check for duplicate name (excluding current department)
        if (!department.getDepartmentName().equals(departmentDTO.getDepartmentName()) &&
            departmentRepository.existsByDepartmentName(departmentDTO.getDepartmentName())) {
            throw new RuntimeException("Department with name '" + departmentDTO.getDepartmentName() + "' already exists");
        }
        
        // Check for duplicate code (excluding current department)
        if (departmentDTO.getDepartmentCode() != null &&
            !departmentDTO.getDepartmentCode().equals(department.getDepartmentCode()) &&
            departmentRepository.existsByDepartmentCode(departmentDTO.getDepartmentCode())) {
            throw new RuntimeException("Department with code '" + departmentDTO.getDepartmentCode() + "' already exists");
        }
        
        department.setDepartmentName(departmentDTO.getDepartmentName());
        department.setDepartmentCode(departmentDTO.getDepartmentCode());
        department.setDescription(departmentDTO.getDescription());
        
        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department updated: {}", updatedDepartment.getDepartmentName());
        
        return mapDepartmentToDTO(updatedDepartment);
    }
    
    /**
     * Get department by ID
     */
    public DepartmentDTO getDepartmentById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
        return mapDepartmentToDTO(department);
    }
    
    /**
     * Get all departments
     */
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapDepartmentToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete department
     */
    @Transactional
    public void deleteDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
        
        departmentRepository.delete(department);
        log.info("Department deleted: {}", department.getDepartmentName());
    }
    
    /**
     * Map Department entity to DTO
     */
    private DepartmentDTO mapDepartmentToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        dto.setDepartmentCode(department.getDepartmentCode());
        dto.setDescription(department.getDescription());
        return dto;
    }

    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please upload a non-empty Excel file");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
            throw new RuntimeException("Only .xlsx or .xls files are supported");
        }
    }
}
