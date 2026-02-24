package com.example.facultyload.service;

import com.example.facultyload.dto.department.DepartmentCreateRequest;
import com.example.facultyload.dto.department.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentCreateRequest request);
    DepartmentResponse update(Long id, DepartmentCreateRequest request);
    DepartmentResponse get(Long id);
    List<DepartmentResponse> list();
}

