package com.example.facultyload.dto.timetable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TimetableCreateRequest(
        @NotNull Long departmentId,
        @NotNull Integer yearOfStudy,
        @NotBlank @Size(max = 10) String section,
        @NotNull Integer semester,
        @Valid List<TimetableEntryRequest> entries
) {
}

