package com.example.facultyload.dto.venue;

import com.example.facultyload.entity.VenueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VenueCreateRequest(
        @NotNull Long departmentId,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 30) String code,
        @NotNull VenueType type
) {
}

