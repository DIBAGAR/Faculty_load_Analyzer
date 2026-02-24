package com.example.facultyload.service;

import com.example.facultyload.dto.venue.VenueCreateRequest;
import com.example.facultyload.dto.venue.VenueResponse;

import java.util.List;

public interface VenueService {
    VenueResponse create(VenueCreateRequest request);
    List<VenueResponse> listByDepartment(Long departmentId);
}

