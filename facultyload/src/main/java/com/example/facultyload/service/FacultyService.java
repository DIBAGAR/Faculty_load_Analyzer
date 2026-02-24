package com.example.facultyload.service;

import com.example.facultyload.dto.faculty.AssignHodRequest;
import com.example.facultyload.dto.faculty.FacultyCreateRequest;
import com.example.facultyload.dto.faculty.FacultyResponse;

import java.util.List;

public interface FacultyService {
    FacultyResponse create(FacultyCreateRequest request);
    FacultyResponse get(Long id);
    List<FacultyResponse> listByDepartment(Long departmentId);
    FacultyResponse assignHod(Long departmentId, AssignHodRequest request);
}

