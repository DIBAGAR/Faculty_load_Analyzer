package com.example.facultyload.dto.faculty;

import jakarta.validation.constraints.NotNull;

public record AssignHodRequest(
        @NotNull Long facultyId
) {
}

