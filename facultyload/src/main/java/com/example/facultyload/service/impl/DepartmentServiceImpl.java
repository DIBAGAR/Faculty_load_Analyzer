package com.example.facultyload.service.impl;

import com.example.facultyload.dto.department.DepartmentCreateRequest;
import com.example.facultyload.dto.department.DepartmentResponse;
import com.example.facultyload.entity.Department;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.DepartmentRepository;
import com.example.facultyload.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new BadRequestException("Department already exists: " + request.name());
        }

        Department dept = Department.builder()
                .name(request.name().trim())
                .code(request.code() == null ? null : request.code().trim())
                .build();
        Department saved = departmentRepository.save(dept);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentCreateRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found: " + id));

        dept.setName(request.name().trim());
        dept.setCode(request.code() == null ? null : request.code().trim());
        Department saved = departmentRepository.save(dept);
        return toResponse(saved);
    }

    @Override
    public DepartmentResponse get(Long id) {
        return departmentRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Department not found: " + id));
    }

    @Override
    public List<DepartmentResponse> list() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    private DepartmentResponse toResponse(Department dept) {
        // ModelMapper is available, but this response has nested IDs; map explicitly.
        return new DepartmentResponse(
                dept.getId(),
                dept.getName(),
                dept.getCode(),
                dept.getHod() != null ? dept.getHod().getId() : null,
                dept.getTempHod() != null ? dept.getTempHod().getId() : null
        );
    }
}

