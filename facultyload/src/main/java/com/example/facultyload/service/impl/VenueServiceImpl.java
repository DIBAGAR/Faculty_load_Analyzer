package com.example.facultyload.service.impl;

import com.example.facultyload.dto.venue.VenueCreateRequest;
import com.example.facultyload.dto.venue.VenueResponse;
import com.example.facultyload.entity.Department;
import com.example.facultyload.entity.Venue;
import com.example.facultyload.exception.BadRequestException;
import com.example.facultyload.exception.NotFoundException;
import com.example.facultyload.repository.DepartmentRepository;
import com.example.facultyload.repository.VenueRepository;
import com.example.facultyload.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public VenueResponse create(VenueCreateRequest request) {
        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found: " + request.departmentId()));

        venueRepository.findByDepartmentIdAndCodeIgnoreCase(dept.getId(), request.code().trim())
                .ifPresent(v -> {
                    throw new BadRequestException("Venue code already exists in department: " + request.code());
                });

        Venue saved = venueRepository.save(Venue.builder()
                .department(dept)
                .name(request.name().trim())
                .code(request.code().trim())
                .type(request.type())
                .isActive(true)
                .build());

        return toResponse(saved);
    }

    @Override
    public List<VenueResponse> listByDepartment(Long departmentId) {
        return venueRepository.findByDepartmentIdAndIsActiveTrue(departmentId).stream().map(this::toResponse).toList();
    }

    private VenueResponse toResponse(Venue v) {
        return new VenueResponse(v.getId(), v.getDepartment().getId(), v.getName(), v.getCode(), v.getType(), v.isActive());
    }
}

