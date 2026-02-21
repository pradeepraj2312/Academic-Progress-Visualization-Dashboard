package com.jd.apvd.service;

import com.jd.apvd.entity.Student;
import com.jd.apvd.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    
    public Optional<Student> getStudentByUserId(String userId) {
        return studentRepository.findByUserId(userId);
    }
}

