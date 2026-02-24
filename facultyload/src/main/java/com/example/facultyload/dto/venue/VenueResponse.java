package com.example.facultyload.dto.venue;

import com.example.facultyload.entity.VenueType;

public record VenueResponse(
        Long id,
        Long departmentId,
        String name,
        String code,
        VenueType type,
        boolean isActive
) {
}

