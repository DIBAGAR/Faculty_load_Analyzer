package com.example.facultyload.controller;

import com.example.facultyload.dto.faculty.AssignHodRequest;
import com.example.facultyload.dto.faculty.FacultyCreateRequest;
import com.example.facultyload.dto.faculty.FacultyResponse;
import com.example.facultyload.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @PreAuthorize("hasRole('FACULTY_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public FacultyResponse create(@Valid @RequestBody FacultyCreateRequest request) {
        return facultyService.create(request);
    }

    @GetMapping("/{id}")
    public FacultyResponse get(@PathVariable Long id) {
        return facultyService.get(id);
    }

    @GetMapping("/department/{departmentId}")
    public List<FacultyResponse> listByDepartment(@PathVariable Long departmentId) {
        return facultyService.listByDepartment(departmentId);
    }

    @PreAuthorize("hasRole('FACULTY_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping("/department/{departmentId}/assign-hod")
    public FacultyResponse assignHod(@PathVariable Long departmentId, @Valid @RequestBody AssignHodRequest request) {
        return facultyService.assignHod(departmentId, request);
    }
}

