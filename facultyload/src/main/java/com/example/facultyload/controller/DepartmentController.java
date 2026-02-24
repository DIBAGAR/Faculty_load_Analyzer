package com.example.facultyload.controller;

import com.example.facultyload.dto.department.DepartmentCreateRequest;
import com.example.facultyload.dto.department.DepartmentResponse;
import com.example.facultyload.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PreAuthorize("hasRole('DEPARTMENT_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public DepartmentResponse create(@Valid @RequestBody DepartmentCreateRequest request) {
        return departmentService.create(request);
    }

    @PreAuthorize("hasRole('DEPARTMENT_ADMIN') or hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentCreateRequest request) {
        return departmentService.update(id, request);
    }

    @GetMapping("/{id}")
    public DepartmentResponse get(@PathVariable Long id) {
        return departmentService.get(id);
    }

    @GetMapping
    public List<DepartmentResponse> list() {
        return departmentService.list();
    }
}

