package com.example.facultyload.controller;

import com.example.facultyload.dto.venue.VenueCreateRequest;
import com.example.facultyload.dto.venue.VenueResponse;
import com.example.facultyload.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PreAuthorize("hasRole('VENUE_ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public VenueResponse create(@Valid @RequestBody VenueCreateRequest request) {
        return venueService.create(request);
    }

    @GetMapping("/department/{departmentId}")
    public List<VenueResponse> listByDepartment(@PathVariable Long departmentId) {
        return venueService.listByDepartment(departmentId);
    }
}

