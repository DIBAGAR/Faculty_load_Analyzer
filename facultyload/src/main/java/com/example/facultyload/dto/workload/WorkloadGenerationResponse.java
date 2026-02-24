package com.example.facultyload.dto.workload;

import java.time.LocalDate;

public record WorkloadGenerationResponse(
        LocalDate weekStartDate,
        int assignmentsCreated,
        int slotsUnassigned
) {
}

